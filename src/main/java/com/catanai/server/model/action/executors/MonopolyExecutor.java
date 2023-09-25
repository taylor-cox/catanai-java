package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Class which validates and executes play monopoly action in game of Catan.
 */
public class MonopolyExecutor implements SpecificActionExecutor {
  private final Game game;

  public MonopolyExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.BUSINESS_AS_USUAL || currentActionState == ActionState.ROLL_DICE) {
      return this.playMonopoly(amd, p);
    } else {
      return false;
    }
  }

  private boolean playMonopoly(ActionMetadata amd, Player p) {
    if (!p.hasDevelopmentCard(DevelopmentCard.MONOPOLY)) { // Ensure player has monopoly card.
      return false;
    } else if (p.hasPlayedDevelopmentCardThisTurn()) { // Ensure player has not played a dev card this turn.
      return false;
    }

    // Get the resource card type to monopolize.
    ResourceCard resourceCard = ResourceCard.valueOf(amd.getRelevantMetadata()[0]);

    if (resourceCard == null) {
      return false;
    }

    // Get all the cards of that type from all players, and add them to the current player's hand.
    for (Player player : this.game.getPlayers()) {
      if (!p.equals(player)) {
        p.addAmountToKnownCards(resourceCard, player.removeAllOfOneCardFromHand(resourceCard));
      }
    }

    // Remove the monopoly card from the player's hand and set the played dev card flag.
    p.removeDevelopmentCard(DevelopmentCard.MONOPOLY);
    p.setPlayedDevelopmentCardThisTurn(true);
    return true;
  }
}
