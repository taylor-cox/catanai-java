package com.catanai.server.model.board.building;

import com.catanai.server.model.player.PlayerID;

/**
* Represents a city by a player.
*/
public final class City extends Building {
  public City(int placement, PlayerID playerId) {
    this.placement = placement;
    this.playerId = playerId;
  }
}
