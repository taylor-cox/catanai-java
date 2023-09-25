package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Class which validates and executes discard action in game of Catan.
 */
public class DiscardExecutor implements SpecificActionExecutor {
  private final Game game;

  public DiscardExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.DISCARD) {
      return this.discard(amd, p);
    } else {
      return false;
    }
  }

  private boolean discard(ActionMetadata amd, Player p) {
    int amountOfCardsInHand = p.getAmountOfResourceCardsInHand();
    int[] relevantMetadata = amd.getRelevantMetadata();
    int amountOfCardsAttemptingToDiscard = 0;
    int amountOfCardsToDiscard = (int) Math.floor(amountOfCardsInHand / 2.0f);

    // Ensure the player must discard.
    if (amountOfCardsInHand < 8) {
      return false;
    } else if (p.hasDiscardedThisTurn()) {
      return false;
    } else if (game.getLastDiceRollValue() != 7) {
      return false;
    }

    // Ensure the player has the cards they are attempting to discard.
    for (int i = 0; i < 5; i++) {
      ResourceCard card = ResourceCard.valueOf(i);
      if (!p.hasAmountOfResourceInHand(card, relevantMetadata[i])) {
        return false;
      }
      amountOfCardsAttemptingToDiscard += relevantMetadata[i];
    }

    // Ensure the player is discarding the correct amount of cards.
    if (amountOfCardsAttemptingToDiscard != amountOfCardsToDiscard) {
      return false;
    }

    // Discard the cards.
    for (int i = 0; i < 5; i++) {
      ResourceCard card = ResourceCard.valueOf(i);
      p.removeAmountOfResourceCardFromHand(card, relevantMetadata[i]);
      for (int j = 0; j < relevantMetadata[i]; j++) {
        this.game.getDealer().returnResource(card);
      }
    }

    p.setHasDiscardedThisTurn(true);
    return true;
  }
}
