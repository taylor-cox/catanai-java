package com.catanai.server.model.board;

import com.catanai.server.model.board.building.City;
import com.catanai.server.model.board.building.Road;
import com.catanai.server.model.board.building.Settlement;
import com.catanai.server.model.board.graph.Edge;
import com.catanai.server.model.board.graph.Node;
import com.catanai.server.model.board.graph.NodeMapper;
import com.catanai.server.model.board.tile.Tile;
import com.catanai.server.model.board.tile.TileGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

/**
* Represents the full game board.
*/
public final class Board {
  private static final int numNodes = 54;
  private static final int numEdges = 72;

  @Getter
  private List<Node> nodes;

  @Getter
  private List<Edge> edges;

  @Getter
  private List<Tile> tiles;

  @Getter
  private int tileIndexCurrentlyBlocked;

  @Getter
  private List<Node> portNodes;
  
  /**
   * Constructs a board with all nodes, edges and tiles.
   */
  public Board() {
    // Initialize nodes.
    this.nodes = new ArrayList<Node>(numNodes);
    for (int i = 0; i < numNodes; i++) {
      nodes.add(new Node(i));
    }

    // Initialize edges.
    this.edges = new ArrayList<Edge>(numEdges);
    for (int i = 0; i < numEdges; i++) {
      edges.add(new Edge(i));
    }

    this.tiles = new TileGenerator().getRandomizedTiles();

    for (int i = 0; i < this.tiles.size(); i++) {
      Tile t = this.tiles.get(i);
      if (t.isBlocked()) {
        this.tileIndexCurrentlyBlocked = i;
        break;
      }
    }

    // Map nodes to edges
    NodeMapper nm = new NodeMapper();
    nm.mapNodesToEdges(nodes, edges);

    // Map nodes to ports.
    nm.mapNodesToPorts(nodes);

    // Map nodes to tiles.
    nm.mapNodesToTiles(nodes, tiles);

    // Initialize port nodes.
    this.portNodes = new ArrayList<Node>();
    this.populatePortNodes();
  }

  /**
   * Populates the port nodes variable with all the nodes which have ports.
   */
  private void populatePortNodes() {
    for (Node n : this.nodes) {
      if (n.hasPort()) {
        this.portNodes.add(n);
      }
    }
  }

  /**
   * Attempts placing road on the board, and returns true if placement successful.
   *
   * @param road road to place on board
   * @return whether road placement was successful
   */
  public boolean placeRoad(Road road) {
    Edge curEdge = this.edges.get(road.getPlacement());
    if (this.canPlaceRoad(road)) {
      curEdge.setRoad(road);
      return true;
    }
    return false;
  }

  /**
   * Contains logic for determining whether or not a road can be placed.
   *
   * @param road road attempting to be placed.
   * @return whether the placement was succesful or not.
   */
  public boolean canPlaceRoad(Road road) {
    Edge curEdge = this.edges.get(road.getPlacement());
    // If the current edge already has a road, cannot place road.
    if (curEdge.hasRoad()) {
      return false;
    }

    // Check if any of the surrounding nodes have a settlement by the same player.
    List<Node> curEdgeNodes = curEdge.getNodes();
    boolean hasBuildingOneNodeAway = curEdgeNodes
        .stream()
        .anyMatch(
          (node) -> node.hasBuilding()
            && node.getBuilding().getPlayerId() == road.getPlayerId()
        );
    if (hasBuildingOneNodeAway) {
      return true;
    }

    // Check if any of the surrounding edges have a road by the same player.
    List<Edge> oneEdgeAway = curEdgeNodes.stream()
        .map((node) -> node.getConnectedEdges())
        .flatMap(List::stream)
        .filter((edge) -> !edge.equals(curEdge))
        .collect(Collectors.toList());
    
    return oneEdgeAway.stream()
        .anyMatch((edge) -> edge.hasRoad() && edge.getRoad().getPlayerId() == road.getPlayerId());
  }

  /**
   * Attempts placing settlement on the board, and retuns true if placement
   * successful.
   *
   * @param settlement settlement to place
   * @return if placement was successful
   */
  public boolean placeSettlement(Settlement settlement) {
    Node curNode = this.nodes.get(settlement.getPlacement());
    if (this.canPlaceSettlement(settlement)) {
      curNode.setBuilding(settlement);
      return true;
    }
    return false;
  }

  /**
   * Contains logic for determining whether or not the given settlement
   * can be placed.
   *
   * @param settlement settlement attemted to be placed
   * @return whether the settlement was successful or not.
   */
  public boolean canPlaceSettlement(Settlement settlement) {
    Node curNode = this.nodes.get(settlement.getPlacement());
    // Check if attempted placement node has a building
    if (curNode.hasBuilding()) {
      return false;
    }

    // Check if there are any settlements within 1 edge of the given node.
    List<Edge> connectedEdges = curNode.getConnectedEdges();
    boolean buildingOneNodeAway = connectedEdges
        .stream()
        .map((edge) -> edge.getNodes())
        .flatMap(List::stream)
        .collect(Collectors.toList())
        .stream()
        .filter((node) -> !node.equals(curNode))
        .anyMatch((node) -> node.hasBuilding());

    if (buildingOneNodeAway) {
      return false;
    }

    if (settlement.isInitialPlacement()) {
      return true;
    }

    // Check if there is a road nearby, if not first settlement
    return connectedEdges.stream()
        .anyMatch(
          (edge) -> edge.hasRoad() && edge.getRoad().getPlayerId() == settlement.getPlayerId()
        );
  }

  /**
   * Attempts placing city on the board, and returns true if placement 
   * successful.
   *
   * @param city city to place
   * @return whether placement was successful or not
   */
  public boolean placeCity(City city) {
    Node curNode = this.nodes.get(city.getPlacement());
    if (this.canPlaceCity(city)) {
      curNode.setBuilding(city);
      return true;
    }
    return false;
  }

  /**
   * Contains logic for determining whether or not a city can be placed.
   *
   * @param city city attempting to be placed.
   * @return whether or not the city can be placed.
   */
  public boolean canPlaceCity(City city) {
    Node curNode = this.nodes.get(city.getPlacement());

    return curNode.hasBuilding()
        && curNode.getBuilding() instanceof Settlement
        && curNode.getBuilding().getPlayerId() == city.getPlayerId();
  }

  /**
   * Attempts placing the robber on the given tile index.
   *
   * @param tileIndex tile to place robber on.
   * @return whether the placement was successful or not
   */
  public boolean placeRobber(int tileIndex) {
    if (this.tileIndexCurrentlyBlocked == tileIndex) {
      return false;
    }
    this.tiles.get(this.tileIndexCurrentlyBlocked).setBlocked(false);
    this.tiles.get(tileIndex).setBlocked(true);
    this.tileIndexCurrentlyBlocked = tileIndex;
    return true;
  }
}
