package com.catanai.server.model.player.action;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
* Represents the possible actions a player can take during their turn.
*/
public enum Action {
  // Build
  PLAY_ROAD(1), PLAY_SETTLEMENT(2), PLAY_CITY(3),
  // Play development cards
  PLAY_KNIGHT(4), PLAY_ROAD_BUILDING(5), PLAY_YEAR_OF_PLENTY(6), PLAY_MONOPOLY(7),
  // Draw development card
  DRAW_DEVELOPMENT_CARD(8),
  // Trade
  OFFER_TRADE(9), ACCEPT_TRADE(10), DECLINE_TRADE(11),
  // Move robber.
  MOVE_ROBBER(12),
  // Discard cards (7 rolled and 8+ cards in hand.)
  DISCARD(13),
  // End turn
  END_TURN(14);
  
  private final int value;
  
  private Action(int value) {
    this.value = value;
  }
  
  public int getValue() {
    return this.value;
  }

  public static Action valueOf(int value) {
    if (value < 1 || value > 14) {
      return null;
    }
    return Stream.of(values())
      .filter((action) -> action.getValue() == value)
      .collect(Collectors.toList())
      .get(0);
  }
}
