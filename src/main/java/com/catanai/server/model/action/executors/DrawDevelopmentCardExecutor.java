package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Class which validates and executes draw development card action in game of Catan.
 */
public class DrawDevelopmentCardExecutor implements SpecificActionExecutor {
  private final Game game;

  public DrawDevelopmentCardExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.BUSINESS_AS_USUAL) {
      return this.drawDevelopmentCard(p);
    } else {
      return false;
    }
  }

  private boolean drawDevelopmentCard(Player p) {
    boolean hasOre = p.hasAmountOfResourceInHand(ResourceCard.ORE, 1);
    boolean hasGrain = p.hasAmountOfResourceInHand(ResourceCard.GRAIN, 1);
    boolean hasWool = p.hasAmountOfResourceInHand(ResourceCard.WOOL, 1);

    
    if (!hasOre || !hasGrain || !hasWool) { // Check if the player has the cards required for dev card.
      return false;
    } else if (
        this.game.getDealer().getDevelopmentBank().getCurrentBankSize() < 1
    ) { // Check that the bank has development cards left to draw.
      return false;
    }

    // Draw a development card.
    p.addDevelopmentCard(this.game.getDealer().drawDevelopmentCard());

    // Remove resources for development card from player's hand.
    p.removeAmountOfResourceCardFromHand(ResourceCard.ORE, 1);
    p.removeAmountOfResourceCardFromHand(ResourceCard.GRAIN, 1);
    p.removeAmountOfResourceCardFromHand(ResourceCard.WOOL, 1);
    return true;
  }
}
