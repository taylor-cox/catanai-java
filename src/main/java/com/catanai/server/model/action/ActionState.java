package com.catanai.server.model.action;

/**
 * Represents the action state of a game of Catan.
 * Determines which actions can be executed at this state.
 */
public enum ActionState {
  /** Starting turns. */
  FIRST_SETTLEMENT(0),
  FIRST_ROAD(1),
  SECOND_SETTLEMENT(2),
  SECOND_ROAD(3),
  /** Usual gameplay states. */
  BUSINESS_AS_USUAL(4),
  /** Starting new turn. */
  ROLL_DICE(5),
  /** If the roll was a 7 and there are players who must discard. */
  DISCARD(6),
  /** If the roll was a 7 and all players have discarded / no players have to discard. */
  MOVE_ROBBER(7),
  TRADE(8),
  /** If any player has 10 or more victory points. */
  FINISHED(9);

  private final int value;

  ActionState(int value) {
    this.value = value;
  }

  /**
   * Returns the integer value of the action state.
   *
   * @return integer value of the action state.
   */
  public int getValue() {
    return this.value;
  }
}
