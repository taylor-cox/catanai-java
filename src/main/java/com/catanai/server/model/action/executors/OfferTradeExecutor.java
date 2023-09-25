package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.action.TradeOffer;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.PlayerID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;


/**
 * Class which validates and executes offer trade action in game of Catan.
 */
public final class OfferTradeExecutor implements SpecificActionExecutor {
  private final Game game;

  public OfferTradeExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.BUSINESS_AS_USUAL) {
      return this.offerInitialTrade(amd, p);
    } else if (currentActionState == ActionState.TRADE) {
      return this.offerCounterTrade(amd, p);
    } else {
      return false;
    }
  }

  private boolean offerInitialTrade(ActionMetadata amd, Player p) {
    // Check if this player has the cards required for the trade, and populate offering cards.
    Map<ResourceCard, Integer> currentPlayerResourceCards = p.getResourceCards();
    Map<ResourceCard, Integer> offerCards = new HashMap<>();
    for (int i = 0; i < 5; i++) {
      ResourceCard card = ResourceCard.valueOf(i);
      if (amd.getRelevantMetadata()[i] > currentPlayerResourceCards.get(card)) {
        return false;
      }
      offerCards.put(card, amd.getRelevantMetadata()[i]);
    }

    // Populate cards the player would like to receive from the trade.
    Map<ResourceCard, Integer> receiveCards = new HashMap<>();
    for (int i = 0; i < 5; i++) {
      ResourceCard card = ResourceCard.valueOf(i);
      receiveCards.put(card, amd.getRelevantMetadata()[i + 5]);
    }
    
    // Create trade offers, and add them to game.
    this.game.getPlayers().stream()
        .filter(player -> player.getID() != p.getID())
        .forEach(player -> 
          this.game.addTradeOffer(
            new TradeOffer(p.getID(), offerCards, player.getID(), receiveCards)
          )
    );

    return true;
  }

  private boolean offerCounterTrade(ActionMetadata amd, Player p) {
    // Check if this player has the cards required for the trade, and populate offering cards.
    Map<ResourceCard, Integer> currentPlayerResourceCards = p.getResourceCards();
    Map<ResourceCard, Integer> offerCards = new HashMap<>();
    for (int i = 0; i < 5; i++) {
      ResourceCard card = ResourceCard.valueOf(i);
      if (amd.getRelevantMetadata()[i] > currentPlayerResourceCards.get(card)) {
        return false;
      }
      offerCards.put(card, amd.getRelevantMetadata()[i]);
    }


    // Populate cards the player would like to receive from the trade.
    Map<ResourceCard, Integer> receiveCards = new HashMap<>();
    for (int i = 0; i < 5; i++) {
      ResourceCard card = ResourceCard.valueOf(i);
      receiveCards.put(card, amd.getRelevantMetadata()[i + 5]);
    }
    
    // Create trade offer to player who's turn it is, and add it to game.
    List<PlayerID> currentTurnPlayers = this.game.getTradeOffers().stream()
        .filter(offer -> offer.getPlayerReceiving() == p.getID())
        .map(TradeOffer::getPlayerOffering)
        .collect(Collectors.toList());
    
    if (currentTurnPlayers.size() != 1) {
      throw new RuntimeException("Something went terribly wrong in OfferTradeExecutor: offerCounterTrade.");
    }

    this.game.addTradeOffer(new TradeOffer(p.getID(), offerCards, currentTurnPlayers.get(0), receiveCards));

    return true;
  }
}
