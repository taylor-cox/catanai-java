package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.building.Road;
import com.catanai.server.model.board.building.Settlement;
import com.catanai.server.model.board.graph.Edge;
import com.catanai.server.model.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Class which validates and executes build road action in game of Catan.
 */
public class RoadExecutor implements SpecificActionExecutor {
  private final Game game;

  public RoadExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    // Check if player is attempting to place first or second road.
    if (currentActionState == ActionState.FIRST_ROAD || currentActionState == ActionState.SECOND_ROAD) {
      return this.startingRoad(amd, p);
    }

    // Check if player is attempting to place a regular road.
    if (currentActionState == ActionState.BUSINESS_AS_USUAL) {
      return this.regularRoad(amd, p);
    }

    return false;
  }

  private boolean startingRoad(@NotNull ActionMetadata amd, @NotNull Player p) {
    int roadPlacement = amd.getRelevantMetadata()[0];
    Edge attemptedRoadEdge = this.game.getBoard().getEdges().get(roadPlacement);
    Road attemptedRoad = new Road(roadPlacement, p.getID(), true);

    // Check that road placement is on board.
    if (roadPlacement > 71) {
      return false;
    }
    // Check that road placement is in player's possible road placements.
    if (p.possibleRoadEdgesDoesNotContain(attemptedRoadEdge)) {
      return false;
    }
    // Check that road can be placed on board.
    if (!this.game.getBoard().canPlaceRoad(attemptedRoad)) {
      return false;
    }

    // Place the road on the board.
    this.game.getBoard().placeRoad(attemptedRoad);

    // Remove 1 road from the player.
    p.setRemainingRoads(p.getRemainingRoads() - 1);

    // Remove the edge from the player's possible road edges.
    p.removeEdgeFromPossibleRoadEdges(attemptedRoadEdge);

    // Update edges on the board where the player can place roads.
    p.clearPossibleRoadEdges();
    this.game.getBoard().getEdges().stream()
      .map(e -> new Road(e.getIndex(), p.getID()))
      .filter(r -> this.game.getBoard().canPlaceRoad(r))
      .forEach(r -> p.addEdgeToPossibleRoadEdges(this.game.getBoard().getEdges().get(r.getPlacement())));
    
    // Update nodes on the board where the player can place settlements.
    p.clearPossibleSettlementNodes();
    this.game.getBoard().getNodes().stream()
      .map(n -> new Settlement(n.getIndex(), p.getID(), false))
      .filter(s -> this.game.getBoard().canPlaceSettlement(s))
      .forEach(s -> p.addNodeToPossibleSettlementNodes(this.game.getBoard().getNodes().get(s.getPlacement()))); 
    
    // Add road to player's roads.
    p.addRoad(attemptedRoad);

    return true;
  }

  private boolean regularRoad(@NotNull ActionMetadata amd, @NotNull Player p) {
    int roadIndex = amd.getRelevantMetadata()[0];
    boolean hasBrick = p.hasAmountOfResourceInHand(ResourceCard.BRICK, 1);
    boolean hasLumber = p.hasAmountOfResourceInHand(ResourceCard.LUMBER, 1);
    final Road roadToPlace = new Road(roadIndex, p.getID());
    Edge edgeToPlaceRoadOn = this.game.getBoard().getEdges().get(roadIndex);

    // Ensure player has brick and lumber for road.
    if (!hasBrick || !hasLumber) {
      return false;
    }
    // Ensure player has roads remaining.
    if (p.getRemainingRoads() < 1) {
      return false;
    }
    // Ensure the player's possible road placements includes the road they are attempting to place.
    if (p.possibleRoadEdgesDoesNotContain(edgeToPlaceRoadOn)) {
      return false;
    }
    // Ensure the road can be placed on the board.
    if (!this.game.getBoard().canPlaceRoad(roadToPlace)) {
      return false;
    }

    // Place the road on the board
    this.game.getBoard().placeRoad(roadToPlace);

    // Remove resources from player.
    p.removeAmountOfResourceCardFromHand(ResourceCard.BRICK, 1);
    p.removeAmountOfResourceCardFromHand(ResourceCard.LUMBER, 1);

    // Remove one road from player.
    p.setRemainingRoads(p.getRemainingRoads() - 1);

    // Remove edge from possible road edges.
    p.removeEdgeFromPossibleRoadEdges(edgeToPlaceRoadOn);

    // Add road to player's roads.
    p.addRoad(roadToPlace);

    // Check if the player has the longest road.
    if (this.playerHasLongestRoad(p)) {
      p.setLongestRoad(true);
    }
    return true;
  }

  private boolean playerHasLongestRoad(@NotNull Player p) {
    // Select a random road of a player.
    List<Edge> playerRoadEdges = p.getRoads().stream()
        .map(r -> this.game.getBoard().getEdges().get(r.getPlacement()))
        .collect(Collectors.toList());

    List<Integer> pathLengths = new ArrayList<>();

    for (Edge e : playerRoadEdges) {
      List<Edge> visitedEdges = new ArrayList<>();
      pathLengths.addAll(this.findPathLength(e, visitedEdges));
    }

    Optional<Integer> maxPathLength = pathLengths.stream()
            .map(Integer::intValue)
            .max((o1, o2) -> Integer.compare(o1, o2));

    if (maxPathLength.isEmpty()) {
      return false;
    }

    for (Player player : this.game.getPlayers()) {
      // Check to see if the given player is the current player
      if (p == player) {
        continue;
      }
      if (maxPathLength.get() > player.getLongestRoadSize()) {
        p.setVictoryPoints(p.getVictoryPoints() + 2);
        p.setLongestRoad(true);

        player.setVictoryPoints(p.getVictoryPoints() - 2);
        p.setLongestRoad(false);
        return true;
      }
    }

    return false;
  }

  private List<Integer> findPathLength(@NotNull Edge e, List<Edge> visitedEdges) {
    List<Integer> pathLengths = new ArrayList<Integer>(); 
    List<Edge> connectedEdges = e.getNodes().stream()
        .flatMap(n -> n.getConnectedEdges().stream())
        .filter(edge -> edge != e)
        .filter(Edge::hasRoad)
        .filter(edge -> edge.getRoad().getPlayerId() == e.getRoad().getPlayerId())
        .filter(edge -> !visitedEdges.contains(edge))
        .collect(Collectors.toList());
    
    for (Edge connectedEdge : connectedEdges) {
      if (visitedEdges.contains(connectedEdge)) {
        continue;
      }
      visitedEdges.add(connectedEdge);
      pathLengths.addAll(this.findPathLength(e, visitedEdges).stream().map(i -> i + 1).collect(Collectors.toList()));
    }

    return pathLengths;
  }
}
