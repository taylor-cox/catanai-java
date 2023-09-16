package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.action.TradeOffer;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class which validates and executes decline trade action in game of Catan.
 */
public final class DeclineTradeExecutor implements SpecificActionExecutor {
  private final Game game;

  public DeclineTradeExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.TRADE) {
      return this.declineTrade(amd, p);
    } else {
      return false;
    }
  }

  private boolean declineTrade(ActionMetadata amd, Player p) {
    // Get the trade offers corresponding to the player and the action metadata.
    List<TradeOffer> playerTradeOffers = this.game.getTradeOffers().stream()
        .filter(tradeOffer -> tradeOffer.getPlayerReceiving() == p.getID())
        .filter(tradeOffer -> {
          for (int i = 0; i < 5; i++) {
            ResourceCard rc = ResourceCard.valueOf(i);
            if (tradeOffer.getResourcesOffered().get(rc) != amd.getRelevantMetadata()[i]) {
              return false;
            }
          }
          return true;
        })
        .collect(Collectors.toList());
    
    // Ensure there is only 1 trade offer.
    if (playerTradeOffers.size() != 1) {
      return false;
    }

    TradeOffer tradeOffer = playerTradeOffers.get(0);
    this.game.removeTradeOffer(tradeOffer);
    return true;
  }
}
