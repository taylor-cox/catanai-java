package com.catanai.server.model.board.building;

import com.catanai.server.model.player.PlayerId;

/**
* Represents a player settlement.
*/
public final class Settlement extends Building {
  /** 
  * Whether the placement was at the beginning of the game or not.
  * Starting placements follow different rules than regular placements.
  *
  * @see <a href="https://colonist.io/catan-rules/#initial-placements">Initial Placements</a>
  */
  private boolean initialPlacement;
  
  /**
  * Constructs a new settlement at placement, for the player of playerID.
  *
  * @param placement the node index of where the settlement was placed.
  * @param playerId the player who placed the settlement.
  * @param initialPlacement if the settlement was an initial placement.
  */
  public Settlement(int placement, PlayerId playerId, boolean initialPlacement) {
    this.placement = placement;
    this.playerId = playerId;
    this.initialPlacement = initialPlacement;
  }
  
  public boolean isInitialPlacement() {
    return this.initialPlacement;
  }
}
