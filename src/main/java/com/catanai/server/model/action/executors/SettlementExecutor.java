package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.building.Settlement;
import com.catanai.server.model.board.graph.Node;
import com.catanai.server.model.board.tile.Terrain;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Class which validates and executes build settlement action in game of Catan.
 */
public final class SettlementExecutor implements SpecificActionExecutor {
  private final Game game;

  public SettlementExecutor(Game game) {
    this.game = game;
  }

  /**
   * Executes the build settlement action.
   */
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.FIRST_SETTLEMENT) {
      return this.firstStartingSettlement(amd, p);
    } else if (currentActionState == ActionState.SECOND_SETTLEMENT) {
      return this.secondStartingSettlement(amd, p);
    } else if (currentActionState == ActionState.BUSINESS_AS_USUAL) {
      return this.regularSettlement(amd, p);
    } else {
      return false;
    }
  }

  private boolean firstStartingSettlement(ActionMetadata amd, Player p) {
    int settlementIndex = amd.getRelevantMetadata()[0];
    Settlement attemptedSettlement = new Settlement(settlementIndex, p.getID(), true);
    
    // Check if placement is valid.
    if (invalidPlacement(settlementIndex, attemptedSettlement)) {
      return false;
    }

    // Place settlement on board.
    if (!this.placeSettlement(attemptedSettlement)) {
      throw new RuntimeException("Failed to place settlement on board.");
    }

    // Set settlement as player's first starting settlement.
    p.setFirstStartingSettlement(attemptedSettlement);
    // Update player's metadata pertaining to placing any settlement.
    updatePlayerMetadata(p, attemptedSettlement);

    return true;
  }

  private boolean secondStartingSettlement(ActionMetadata amd, Player p) {
    int settlementIndex = amd.getRelevantMetadata()[0];
    Settlement attemptedSettlement = new Settlement(settlementIndex, p.getID(), true);

    // Check node index is on board.
    if (this.invalidPlacement(settlementIndex, attemptedSettlement)) {
      return false;
    }

    // Place settlement on board.
    if (!this.placeSettlement(attemptedSettlement)) {
      throw new RuntimeException("Failed to place settlement on board.");
    }

    // Add settlement as player's second starting settlement.
    p.setSecondStartingSettlement(attemptedSettlement);

    // Add resources to player's hand from second settlement location.
    this.game.getBoard().getNodes().get(settlementIndex).getProduces()
      .stream()
      .filter(t -> t != Terrain.DESERT)
      .filter(t -> this.game.getDealer().canDrawResource(t, 1))
      .forEach(t -> p.addToKnownCards(this.game.getDealer().drawResource(t)));

    // Update player's metadata pertaining to placing any settlement.
    this.updatePlayerMetadata(p, attemptedSettlement);
    return true;
  }

  private boolean regularSettlement(ActionMetadata amd, Player p) {
    int settlementIndex = amd.getRelevantMetadata()[0];
    Settlement attemptedSettlement = new Settlement(settlementIndex, p.getID(), false);
    
    // Ensure the player has the resource cards in hand for the settlement.
    if (!this.hasResourcesForSettlement(p)) {
      return false;
    }
    // Ensure the settlement placement is on the board.
    if (this.invalidPlacement(settlementIndex, attemptedSettlement)) {
      return false;
    }
    // Ensure the player has a settlement to place.
    if (p.getRemainingSettlements() < 1) {
      return false;
    }
    // Place the settlement on the board.
    if (!this.placeSettlement(attemptedSettlement)) {
      throw new RuntimeException("Failed to place settlement on board.");
    }

    // Remove resource cards from player's hand.
    this.removeSettlementResourcesFromHand(p);

    // Update player's metadata pertaining to placing any settlement.
    this.updatePlayerMetadata(p, attemptedSettlement);

    return true;
  }

  //****************************************************************************
  //*************************** Helper Functions *******************************
  //****************************************************************************

  private boolean invalidPlacement(int nodeIndex, Settlement attemptedSettlement) {
    if (nodeIndex < 0 || nodeIndex > 53) {
      return true;
    }
    return !this.game.getBoard().canPlaceSettlement(attemptedSettlement);
  }

  private boolean placeSettlement(Settlement attemptedSettlement) {
    return this.game.getBoard().placeSettlement(attemptedSettlement);
  }

  private void updatePlayerMetadata(Player p, Settlement attemptedSettlement) {
    // Set remaining settlements to 1 less, and add 1 victory point.
    p.setRemainingSettlements(p.getRemainingSettlements() - 1);
    p.setVictoryPoints(p.getVictoryPoints() + 1);

    // Add the edges connected to this settlement to player's possible road edges.
    p.clearPossibleRoadEdges();
    p.addAllEdgesToPossibleRoadEdges(
        this.game.getBoard().getNodes().get(attemptedSettlement.getPlacement()).getConnectedEdges()
    );

    // Add this node to possible city nodes for the player.
    Node settlementNode = this.game.getBoard().getNodes().get(attemptedSettlement.getPlacement());
    p.addNodeToPossibleCityNodes(settlementNode);

    // Remove node from possible settlement nodes.
    p.removeNodeFromPossibleSettlementNodes(settlementNode);
  }

  private boolean hasResourcesForSettlement(Player p) {
    boolean hasWool = p.hasAmountOfResourceInHand(ResourceCard.WOOL, 1);
    boolean hasBrick = p.hasAmountOfResourceInHand(ResourceCard.BRICK, 1);
    boolean hasGrain = p.hasAmountOfResourceInHand(ResourceCard.GRAIN, 1);
    boolean hasLumber = p.hasAmountOfResourceInHand(ResourceCard.LUMBER, 1);
    return hasWool && hasBrick && hasGrain && hasLumber;
  }

  private void removeSettlementResourcesFromHand(Player p) {
    p.removeAmountOfResourceCardFromHand(ResourceCard.BRICK, 1);
    p.removeAmountOfResourceCardFromHand(ResourceCard.WOOL, 1);
    p.removeAmountOfResourceCardFromHand(ResourceCard.GRAIN, 1);
    p.removeAmountOfResourceCardFromHand(ResourceCard.LUMBER, 1);
  }
}
