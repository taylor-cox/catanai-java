package com.catanai.server.model.board.graph;

import com.catanai.server.model.board.building.Road;
import java.util.ArrayList;

/**
* Represents an edge connecting two roads on the board.
*/
public final class Edge {
  /** Edge has an index from 0-71. */
  private int index;
  /** Represents the nodes connected to the edge. */
  private ArrayList<Node> nodes;
  private Road road;
  
  /**
   * Creates a new edge with the index @param index.
   *
   * @param index index of the edge on the board.
   */
  public Edge(int index) {
    this.index = index;
    this.nodes = new ArrayList<Node>();
    this.road = null;
  }
  
  //****************************************************************************
  //*************************** Getters and setters ****************************
  //****************************************************************************
  
  public boolean hasRoad() {
    return this.road != null;
  }
  
  public Road getRoad() {
    return this.road;
  }
  
  /**
   * Sets road to @param road, and returns whether successful.
   *
   * @param road road to change the edge to.
   * @return whether the road change was successful.
   */
  public boolean setRoad(Road road) {
    if (this.road != null) {
      return false;
    }
    this.road = road;
    return true;
  }
  
  public void addNode(Node node) {
    this.nodes.add(node);
  }
  
  public ArrayList<Node> getNodes() {
    return this.nodes;
  }
  
  public int getIndex() {
    return this.index;
  }
}
