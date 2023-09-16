package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Class which validates and executes end turn action in game of Catan.
 */
public class EndTurnExecutor implements SpecificActionExecutor {
  public EndTurnExecutor(Game game) {
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.BUSINESS_AS_USUAL) {
      return endTurn(p);
    } else {
      return false;
    }
  }

  private boolean endTurn(Player p) {
    p.addAllDevelopmentCardsDrawnThisTurnToDevelopmentCards();
    p.setHasDiscardedThisTurn(false);
    p.setPlayedDevelopmentCardThisTurn(false);
    p.setRolledDiceThisTurn(false);
    return true;
  }
}
