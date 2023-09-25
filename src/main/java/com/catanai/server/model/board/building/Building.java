package com.catanai.server.model.board.building;

import com.catanai.server.model.player.PlayerID;
import lombok.Getter;

/**
* Represents any building in Catan (Road, Settlement, City).
*/
@Getter
public abstract class Building {
  /** The ID of the player who placed the building. */
  protected PlayerID playerId;
  /** The placement of the building, either a node or edge index. */
  protected int placement;
  
  //**************************************************************************
  //*************************** Getters and setters **************************
  //**************************************************************************

  public void setPlayerId(PlayerID playerId) {
    this.playerId = playerId;
  }

  public void setPlacement(int placement) {
    this.placement = placement;
  }
}
