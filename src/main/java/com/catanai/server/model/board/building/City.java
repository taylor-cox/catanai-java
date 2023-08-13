package com.catanai.server.model.board.building;

import com.catanai.server.model.player.PlayerId;

/**
* Represents a city by a player.
*/
public final class City extends Building {
  public City(int placement, PlayerId playerId) {
    this.placement = placement;
    this.playerId = playerId;
  }
}
