package com.catanai.server.model.board.building;

import com.catanai.server.model.player.PlayerId;

/**
* Represents a player road.
*/
public final class Road extends Building {
  private boolean startingPlacement;

  public Road(int placement, PlayerId playerId) {
    this.placement = placement;
    this.playerId = playerId;
  }
  
  /**
   * Constructor for beginning road placement.
   *
   * @param placement placement on the board (edge index)
   * @param playerId player placing road
   * @param startingPlacement whether this is a starting road or not.
   */
  public Road(int placement, PlayerId playerId, boolean startingPlacement) {
    this.placement = placement;
    this.playerId = playerId;
    this.startingPlacement = startingPlacement;
  }

  public boolean isStartingPlacement() {
    return this.startingPlacement;
  }
}
