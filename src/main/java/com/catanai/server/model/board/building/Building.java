package com.catanai.server.model.board.building;

import com.catanai.server.model.player.PlayerId;

/**
* Represents any building in Catan (Road, Settlement, City).
*/
public abstract class Building {
  /** The ID of the player who placed the building. */
  protected PlayerId playerId;
  /** The placement of the building, either a node or edge index. */
  protected int placement;
  
  //**************************************************************************
  //*************************** Getters and setters **************************
  //**************************************************************************
  
  public PlayerId getPlayerId() {
    return this.playerId;
  }
  
  public void setPlayerId(PlayerId playerId) {
    this.playerId = playerId;
  }
  
  public int getPlacement() {
    return this.placement;
  }
  
  public void setPlacement(int placement) {
    this.placement = placement;
  }
}
