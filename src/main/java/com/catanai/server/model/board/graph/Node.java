package com.catanai.server.model.board.graph;

import com.catanai.server.model.board.building.Building;
import com.catanai.server.model.board.tile.Port;
import com.catanai.server.model.board.tile.Terrain;
import java.util.ArrayList;
import java.util.List;

/**
* Represents a node on the board (i.e. anywhere a building may be placed.)
*/
public final class Node {
  private int index;
  private List<Edge> connectedEdges;
  private List<Terrain> produces;
  private Building building;
  private Port port;
  private boolean blocked;
  
  /**
   * Generates a new node at the index @param index.
   *
   * @param index index of the new node.
   */
  public Node(int index) {
    this.index = index;
    this.connectedEdges = new ArrayList<Edge>();
    this.produces = new ArrayList<Terrain>();
    this.building = null;
    this.port = Port.NO_PORT;
    this.blocked = false;
  }
  
  //****************************************************************************
  //*************************** Getters and setters ****************************
  //****************************************************************************
  
  public boolean hasPort() {
    return this.port != Port.NO_PORT;
  }
  
  public int getIndex() {
    return this.index;
  }
  
  public void setIndex(int index) {
    this.index = index;
  }
  
  public List<Edge> getConnectedEdges() {
    return this.connectedEdges;
  }
  
  public void addConnectedEdge(Edge edge) {
    this.connectedEdges.add(edge);
  }
  
  public List<Terrain> getProduces() {
    return this.produces;
  }
  
  public void addProduces(Terrain produces) {
    this.produces.add(produces);
  }
  
  public boolean hasBuilding() {
    return this.building != null;
  }
  
  public Building getBuilding() {
    return this.building;
  }
  
  public void setBuilding(Building building) {
    this.building = building;
  }
  
  public Port getPort() {
    return this.port;
  }
  
  public void setPort(Port port) {
    this.port = port;
  }
  
  public boolean isBlocked() {
    return this.blocked;
  }
  
  public void setBlocked(boolean blocked) {
    this.blocked = blocked;
  }
}
