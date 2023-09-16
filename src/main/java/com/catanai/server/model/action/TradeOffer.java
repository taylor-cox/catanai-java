package com.catanai.server.model.action;

import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.player.PlayerID;
import lombok.Getter;

import java.util.Map;

/**
 * Class tracking trade offer. Contains information about the trade offer, and the players involved.
 */
@Getter
public class TradeOffer {
  // Metadata for player offering the trade.
  private final Map<ResourceCard, Integer> resourcesOffered;
  private final PlayerID playerOffering;
  private Boolean offeringAccepted;

  // Metadata for player receiving the trade.
  private final Map<ResourceCard, Integer> resourcesRequested;
  private final PlayerID playerReceiving;
  private Boolean receivingAccepted;

  /**
   * Generate a trade offer.
   *
   * @param playerOffering player offering the trade.
   * @param offered cards offered by the player.
   * @param playerReceiving player receiving the trade.
   * @param requested cards requested by the offering player from the receiving player.
   */
  public TradeOffer(
      PlayerID playerOffering,
      Map<ResourceCard, Integer> offered,
      PlayerID playerReceiving,
      Map<ResourceCard, Integer> requested
  ) {
    this.playerOffering = playerOffering;
    this.resourcesOffered = offered;

    this.playerReceiving = playerReceiving;
    this.resourcesRequested = requested;
    
    this.offeringAccepted = null;
    this.receivingAccepted = null;
  }

  //****************************************************************************
  //*************************** Getters and Setters ****************************
  //****************************************************************************

  public Boolean awaitingOfferingResponse() {
    return this.receivingAccepted;
  }

  public void setOfferingAccepted(Boolean offeringAccepted) {
    this.offeringAccepted = offeringAccepted;
  }

  public void setReceivingAccepted(Boolean receivingAccepted) {
    this.receivingAccepted = receivingAccepted;
  }
}
