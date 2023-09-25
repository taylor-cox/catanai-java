package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Class which validates and executes roll dice action in game of Catan.
 */
public class RollDiceExecutor implements SpecificActionExecutor {
  private final Game game;

  public RollDiceExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.ROLL_DICE) {
      return this.rollDice(p);
    } else {
      return false;
    }
  }

  private boolean rollDice(Player p) {
    if (p.hasRolledDiceThisTurn()) {
      return false;
    }

    p.setRolledDiceThisTurn(true);
    this.game.rollDice();
    return true;
  }
}
