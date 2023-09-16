package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.action.TradeOffer;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class which validates and executes accept trade action in game of Catan.
 */
public final class AcceptTradeExecutor implements SpecificActionExecutor {
  private final Game game;

  public AcceptTradeExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.TRADE) {
      return this.acceptTrade(amd, p);
    } else {
      return false;
    }
  }

  private boolean acceptTrade(ActionMetadata amd, Player p) {
    // Get the trade offer(s) corresponding to the player and the action metadata.
    List<TradeOffer> playerTradeOffers = this.game.getTradeOffers().stream()
        .filter(tradeOffer -> tradeOffer.getPlayerReceiving() == p.getID())
        .filter(tradeOffer -> {
          for (int i = 0; i < 5; i++) {
            ResourceCard rc = ResourceCard.valueOf(i);
            if (tradeOffer.getResourcesOffered().get(rc) != amd.getRelevantMetadata()[i]) {
              return false;
            }
          }
          for (int i = 0; i < 5; i++) {
            ResourceCard rc = ResourceCard.valueOf(i);
            if (tradeOffer.getResourcesRequested().get(rc) != amd.getRelevantMetadata()[i + 5]) {
              return false;
            }
          }
          return true;
        })
        .collect(Collectors.toList());

    // Ensure there are trade offers.
    if (playerTradeOffers.isEmpty()) {
      return false;
    }

    // Ensure there is only 1 trade offer.
    if (playerTradeOffers.size() > 1) {
      throw new RuntimeException("More than 1 trade offer for player" + p.getID() + ".");
    }

    // Ensure the player has the cards for the trade offer.
    TradeOffer tradeOffer = playerTradeOffers.get(0);
    Map<ResourceCard, Integer> resourcesOffered = tradeOffer.getResourcesOffered();
    Map<ResourceCard, Integer> playerResources = p.getResourceCards();
    for (int i = 0; i < 5; i++) {
      ResourceCard rc = ResourceCard.valueOf(i);
      if (playerResources.get(rc) < resourcesOffered.get(rc)) {
        return false;
      }
    }
    return true;
  }
}
