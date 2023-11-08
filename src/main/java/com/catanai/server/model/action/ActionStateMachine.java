package com.catanai.server.model.action;

import com.catanai.server.model.Game;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.PlayerID;
import java.util.LinkedList;
import java.util.Queue;
import lombok.Getter;

/**
 * Handles state transitions for ActionState.
 */
public final class ActionStateMachine {
  @Getter
  private ActionState currentActionState;
  private final Game game;
  private Player turnPlayer;
  private Queue<Player> playersToDiscard;
  private Queue<Player> playersToGetTradeResponse;

  /**
   * Creates a new ActionStateMachine for a game of Catan.
   *
   * @param game game to create an ActionStateMachine for.
   */
  public ActionStateMachine(Game game) {
    this.currentActionState = ActionState.FIRST_SETTLEMENT;
    this.game = game;
    this.playersToDiscard = null;
    this.playersToGetTradeResponse = null;
  }

  /**
   * Returns the next action state.
   *
   * @return the next action state.
   */
  private ActionState getNextActionState() {
    switch (this.currentActionState) {
      case FIRST_SETTLEMENT:
        return ActionState.FIRST_ROAD;
      case FIRST_ROAD:
        return this.handleActionStateFirstRoad();
      case SECOND_SETTLEMENT:
        return ActionState.SECOND_ROAD;
      case SECOND_ROAD:
        return this.handleActionStateSecondRoad();
      case ROLL_DICE:
        return this.handleActionStateDiceRoll();
      case DISCARD:
        return this.handleActionStateDiscard();
      case MOVE_ROBBER:
        return ActionState.BUSINESS_AS_USUAL;
      case BUSINESS_AS_USUAL:
        return this.handleActionStateBusinessAsUsual();
      case TRADE:
        return this.handleActionStateTrade();
      default:
        throw new RuntimeException("Invalid action state.");
    }
  }

  /**
   * Handles the first road action state.
   * The first road is succeeded by:
   * - First settlement, if there are still players who have not played their
   * first settlement yet.
   * - Second settlement, if all other players have placed their first settlement.
   *
   * @return the next action state.
   */
  private ActionState handleActionStateFirstRoad() {
    // Get how many players have played their first settlement.
    long playersWithFirstStartingSettlement = this.game.getPlayers().stream()
        .filter(p -> p.getFirstStartingSettlement() != null)
        .count();

    // If all players have placed their first settlement, the current player plays
    // their second settlement.
    if (playersWithFirstStartingSettlement >= 4) {
      return ActionState.SECOND_SETTLEMENT;
    }

    // Otherwise, update the current player to be the next player, and set the
    // current
    // action state to the first settlement for that player.
    PlayerID currentPlayerID = this.game.getCurrentPlayer().getID();
    this.game.setCurrentPlayer(this.game.getPlayers().get(currentPlayerID.getValue()));
    return ActionState.FIRST_SETTLEMENT;
  }

  /**
   * Handles the second road action state.
   * The second road is succeeded by:
   * - Second settlement, if there are still players who have not played their
   * second settlement yet.
   * - Roll dice, if all other players have placed their second settlement.
   *
   * @return the next action state.
   */
  private ActionState handleActionStateSecondRoad() {
    // Get how many players have played their second settlement.
    long playersWithSecondStartingSettlement = this.game.getPlayers().stream()
        .filter(p -> p.getSecondStartingSettlement() != null)
        .count();

    // If all players have placed their second settlement, the current player rolls
    // the dice.
    if (playersWithSecondStartingSettlement >= 4) {
      return ActionState.ROLL_DICE;
    }

    // Otherwise, update the current player to be the next player, and set the
    // current
    // action state to the second settlement for that player.
    PlayerID currentPlayerID = this.game.getCurrentPlayer().getID();
    this.game.setCurrentPlayer(this.game.getPlayers().get(currentPlayerID.getValue() - 2));
    return ActionState.SECOND_SETTLEMENT;
  }

  /**
   * Handles the dice roll action state.
   * The dice roll is succeeded by:
   * - Discard, if the dice roll is a 7.
   * - Business as usual, if the dice roll is not a 7.
   *
   * @return the next action state.
   */
  private ActionState handleActionStateDiceRoll() {
    // If the dice roll is not a 7, business as usual.
    if (this.game.getLastDiceRollValue() != 7) {
      return ActionState.BUSINESS_AS_USUAL;
    }

    // If the dice roll is a 7, players must discard and current player must move
    // robber.
    if (!getPlayersToDiscard()) {
      return ActionState.MOVE_ROBBER;
    }
    return ActionState.DISCARD;
  }

  private boolean getPlayersToDiscard() {
    Queue<Player> playersToDiscard = new LinkedList<>();

    for (Player player : this.game.getPlayers()) {
      if (player.getAmountOfResourceCardsInHand() > 7) {
        playersToDiscard.add(player);
      }
    }

    if (playersToDiscard.isEmpty()) {
      this.playersToDiscard = null;
      this.turnPlayer = null;
      return false;
    }

    this.turnPlayer = this.game.getCurrentPlayer();
    this.playersToDiscard = playersToDiscard;
    this.game.setCurrentPlayer(playersToDiscard.poll());
    return true;
  }

  /**
   * Handles the discard action state.
   * The discard action state is succeeded by:
   * - Discard, if there are still players who have not discarded yet.
   * - Move robber, if all players who must discard have discarded.
   *
   * @return the next action state.
   */
  private ActionState handleActionStateDiscard() {
    // Check if any players have to discard, and if so,
    // make those players discard.
    if (this.playersToDiscard == null) {
      return ActionState.MOVE_ROBBER;
    }

    // Get the next player to discard.
    Player playerToDiscard = this.playersToDiscard.poll();

    // If there is a player to discard, require them to discard.
    // Set the game's current player to that player, and return discard action
    // state.
    if (playerToDiscard != null) {
      this.game.setCurrentPlayer(playerToDiscard);
      return ActionState.DISCARD;
    }

    // If there are no more players who must discard, make the current turn player
    // move the robber.
    this.game.setCurrentPlayer(this.turnPlayer);
    this.playersToDiscard = null;
    this.turnPlayer = null;
    return ActionState.MOVE_ROBBER;
  }

  /**
   * Handles the business as usual action state.
   * The business as usual action state is succeeded by:
   * - Roll dice, if the current player's last action was end turn.
   * - Offer trade, if the current player's last action was offer trade.
   * - Business as usual, if the current player's last action was anything else.
   *
   * @return the next action state.
   */
  private ActionState handleActionStateBusinessAsUsual() {
    // If the current player has 10 or more victory points, they win.
    if (this.game.getCurrentPlayer().getVictoryPoints() >= 10) {
      return ActionState.FINISHED;
    }

    // If the current player's last action was end turn, then the next player.
    if (this.game.getCurrentPlayer().getPreviousAction().getAction() == Action.END_TURN) {
      Player nextPlayer = this.game.getPlayers().get(this.game.getCurrentPlayer().getID().getValue() % 4);
      this.game.setCurrentPlayer(nextPlayer);
      return ActionState.ROLL_DICE;
    }

    // If the current player's last action was to offer a trade, throw
    // NotImplementedException.
    if (this.game.getCurrentPlayer().getPreviousAction().getAction() == Action.OFFER_TRADE) {
      return ActionState.TRADE;
    }

    // If the current player's last action was anything else, return business as
    // usual.
    return ActionState.BUSINESS_AS_USUAL;
  }

  /**
   * Handles the trade action state.
   * The trade action state is succeeded by:
   * - Trade, if there are still players who have not responded to the trade yet.
   * - Business as usual, if all players have responded to the trade.
   *
   * @return next action state.
   */
  private ActionState handleActionStateTrade() {
    // Compute players to get trade response from.
    if (this.playersToGetTradeResponse == null) {
      this.playersToGetTradeResponse = new LinkedList<>();

      for (int i = 0; i < 4; i++) {
        Player player = this.game.getPlayers().get(i);
        if (player.getID() != this.game.getCurrentPlayer().getID()) {
          this.playersToGetTradeResponse.add(player);
        }
      }

      this.turnPlayer = this.game.getCurrentPlayer();

      // Add the turn player to the end of the list.
      this.playersToGetTradeResponse.add(this.turnPlayer);
    }

    // Get the next player to get trade response from.
    Player playerToGetTradeResponse = this.playersToGetTradeResponse.poll();

    // Reached end of players to get trades from; business as usual.
    if (playerToGetTradeResponse == null) {
      this.playersToGetTradeResponse = null;
      this.game.setCurrentPlayer(this.turnPlayer);
      return ActionState.BUSINESS_AS_USUAL;
    }

    // If there is a player to get trade response from, require them to respond to
    // trade.
    // Set the game's current player to that player, and return trade action state.
    if (playerToGetTradeResponse.getID() == this.turnPlayer.getID()) {
      this.game.setCurrentPlayer(this.turnPlayer);
    } else {
      this.game.setCurrentPlayer(playerToGetTradeResponse);
    }
    return ActionState.TRADE;
  }

  public void nextActionState() {
    this.currentActionState = this.getNextActionState();
  }

}
