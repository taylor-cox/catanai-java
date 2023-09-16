package com.catanai.server.model.action;

/**
 * Represents the action state of a game of Catan.
 * Determines which actions can be executed at this state.
 */
public enum ActionState {
  /** Starting turns. */
  FIRST_SETTLEMENT,
  FIRST_ROAD,
  SECOND_SETTLEMENT,
  SECOND_ROAD,
  /** Usual gameplay states. */
  BUSINESS_AS_USUAL,
  /** Starting new turn. */
  ROLL_DICE,
  /** If the roll was a 7 and there are players who must discard. */
  DISCARD,
  /** If the roll was a 7 and all players have discarded / no players have to discard. */
  MOVE_ROBBER,
  TRADE,
  /** If any player has 10 or more victory points. */
  FINISHED
}
