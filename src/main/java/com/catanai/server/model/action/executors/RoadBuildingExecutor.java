package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.board.building.Road;
import com.catanai.server.model.board.graph.Edge;
import com.catanai.server.model.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Class which validates and executes play road building action in game of Catan.
 */
public class RoadBuildingExecutor implements SpecificActionExecutor {
  private final Game game;

  public RoadBuildingExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.BUSINESS_AS_USUAL) {
      return this.playRoadBuilding(amd, p);
    } else {
      return false;
    }
  }

  private boolean playRoadBuilding(ActionMetadata amd, Player p) {
    int road1Index = amd.getRelevantMetadata()[0];
    int road2Index = amd.getRelevantMetadata()[1];

    // Validate
    if (p.hasPlayedDevelopmentCardThisTurn()) {
      return false;
    } else if (!p.hasDevelopmentCard(DevelopmentCard.ROAD_BUILDING)) {
      return false;
    } else if (p.getRemainingRoads() < 2) {
      return false;
    }

    Road road1 = new Road(road1Index, p.getID());
    Road road2 = new Road(road2Index, p.getID());

    if (!this.game.getBoard().canPlaceRoad(road1) || !this.game.getBoard().canPlaceRoad(road2)) {
      return false;
    }

    // Check if player has longest road after playing dev card.
    if (playerHasLongestRoad(p)) {
      p.setVictoryPoints(p.getVictoryPoints() + 2);
      p.setLongestRoad(true);

      for (Player player : this.game.getPlayers()) {
        if (p != player && player.hasLongestRoad()) {
          player.setLongestRoad(false);
          player.setVictoryPoints(player.getVictoryPoints() - 2);
        }
      }
    }

    // Remove road building card from player's hand, remove 2 roads from player,
    // set has played dev card this turn.
    p.removeDevelopmentCard(DevelopmentCard.ROAD_BUILDING);
    p.setRemainingRoads(p.getRemainingRoads() - 2);
    p.setPlayedDevelopmentCardThisTurn(true);
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
