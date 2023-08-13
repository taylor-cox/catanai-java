package com.catanai.server.model.board.tile;

import com.catanai.server.model.board.graph.Node;
import java.util.ArrayList;

/**
* Represents a board tile.
*/
public final class Tile {
  private Terrain terrain;
  private TerrainChit terrainChit;
  private ArrayList<Node> nodes;
  private boolean blocked;
  
  /**
   * Constructs a tile with terrain and terrainChit.
   *
   * @param terrain terrain of tile.
   * @param terrainChit terrainChit of tile.
   */
  public Tile(Terrain terrain, TerrainChit terrainChit) {
    this.terrain = terrain;
    this.terrainChit = terrainChit;
    this.blocked = terrain == Terrain.DESERT;
    this.nodes = new ArrayList<Node>();
  }
  
  //****************************************************************************
  //*************************** Getters and setters ****************************
  //****************************************************************************
  
  public Terrain getTerrain() {
    return this.terrain;
  }
  
  public TerrainChit getTerrainChit() {
    return this.terrainChit;
  }
  
  public void addNode(Node node) {
    this.nodes.add(node);
  }
  
  public boolean isBlocked() {
    return this.blocked;
  }
  
  /**
   * Set this tile to either blocked or unblocked.
   *
   * @param blocked whether to block or unblock tile.
   * @return whether the operation was valid for this tile.
   */
  public boolean setBlocked(boolean blocked) {
    if (this.blocked == blocked) {
      return false;
    }
    this.blocked = blocked;
    for (Node node : this.nodes) {
      node.setBlocked(blocked);
    }
    return true;
  }
  
  public ArrayList<Node> getNodes() {
    return this.nodes;
  }
}
