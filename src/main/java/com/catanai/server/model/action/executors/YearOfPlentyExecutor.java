package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Class which validates and executes play year of plenty action in game of Catan.
 */
public class YearOfPlentyExecutor implements SpecificActionExecutor {
  private final Game game;

  public YearOfPlentyExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.BUSINESS_AS_USUAL || currentActionState == ActionState.ROLL_DICE) {
      return this.playYearOfPlenty(amd, p);
    } else {
      return false;
    }
  }

  private boolean playYearOfPlenty(ActionMetadata amd, Player p) {
    boolean hasYearOfPlenty = p.hasDevelopmentCard(DevelopmentCard.YEAR_OF_PLENTY);
    ResourceCard resource1 = ResourceCard.valueOf(amd.getRelevantMetadata()[0]);
    ResourceCard resource2 = ResourceCard.valueOf(amd.getRelevantMetadata()[1]);
    
    // Check the resource cards are valid.
    if (resource1 == null || resource2 == null) {
      return false;
    }

    // Populate whether the dealer has the ability to give the player the resources they requested.
    boolean canDrawResource1;
    boolean canDrawResource2;
    if (resource1 == resource2) {
      canDrawResource1 = this.game.getDealer().canDrawResource(resource1, 2);
      canDrawResource2 = this.game.getDealer().canDrawResource(resource2, 2);
    } else {
      canDrawResource1 = this.game.getDealer().canDrawResource(resource1, 1);
      canDrawResource2 = this.game.getDealer().canDrawResource(resource2, 1);
    }

    // Ensure player has year of plenty card.
    if (!hasYearOfPlenty) {
      return false;
    }

    // Ensure the game dealer can give the two resources the player requested.
    if (!canDrawResource1 || !canDrawResource2) {
      return false;
    }
    
    // Ensure the player has not played a development card this turn.
    if (p.hasPlayedDevelopmentCardThisTurn()) {
      return false;
    }

    // Remove year of plenty card from player's hand, and set has played development card this turn.
    p.removeDevelopmentCard(DevelopmentCard.YEAR_OF_PLENTY);
    p.setPlayedDevelopmentCardThisTurn(true);

    // Add resources to player's hand.
    if (resource1 == resource2) {
      p.addAmountToKnownCards(this.game.getDealer().drawResource(resource1), 2);
    } else {
      p.addAmountToKnownCards(this.game.getDealer().drawResource(resource1), 1);
      p.addAmountToKnownCards(this.game.getDealer().drawResource(resource2), 1);
    }

    return true;
  }
}
