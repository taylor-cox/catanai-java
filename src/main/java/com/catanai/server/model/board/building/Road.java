package com.catanai.server.model.board.building;

import com.catanai.server.model.player.PlayerId;

/**
* Represents a player road.
*/
public final class Road extends Building {
  public Road(int placement, PlayerId playerId) {
    this.placement = placement;
    this.playerId = playerId;
  }
}
