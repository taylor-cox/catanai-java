package com.catanai.server.model.player;

import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.building.Road;
import com.catanai.server.model.board.building.Settlement;
import com.catanai.server.model.board.graph.Edge;
import com.catanai.server.model.board.graph.Node;
import com.catanai.server.model.gamestate.GameState;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
* Represents a Catan player.
*/
public abstract class Player {
  protected PlayerID id;
  @Getter
  protected int victoryPoints;

  @Getter
  protected Map<ResourceCard, Integer> resourceCards;
  @Getter
  protected Map<ResourceCard, Integer> knownCards;

  @Getter
  protected List<DevelopmentCard> developmentCards;
  @Getter
  protected ArrayList<DevelopmentCard> developmentCardsDrawnThisTurn;
  protected boolean playedDevelopmentCardThisTurn;

  @Getter
  protected int remainingSettlements;
  @Getter
  protected int remainingCities;
  @Getter
  protected int remainingRoads;

  @Getter
  protected List<Edge> possibleRoadEdges;
  @Getter
  protected List<Node> possibleSettlementNodes;
  @Getter
  protected List<Node> possibleCityNodes;

  @Getter
  protected List<Road> roads;

  @Getter
  protected int numKnightsPlayed;
  protected boolean largestArmy;
  protected boolean longestRoad;
  @Getter
  protected int longestRoadSize;

  protected boolean rolledDiceThisTurn;
  protected boolean hasFinishedTurn;
  protected boolean hasDiscardedThisTurn;
  @Getter
  protected ActionMetadata previousAction;

  @Getter
  protected Settlement firstStartingSettlement;
  @Getter
  protected Settlement secondStartingSettlement;

  /**
   * Initializes all player variables.
   */
  public Player(PlayerID id) {
    this.id = id;
    this.victoryPoints = 0;

    this.resourceCards = new HashMap<>();
    this.knownCards = new HashMap<>();
    for (int i = 0; i < 5; i++) {
      this.resourceCards.put(ResourceCard.valueOf(i), 0);
      this.knownCards.put(ResourceCard.valueOf(i), 0);
    }

    this.developmentCards = new ArrayList<>();
    this.developmentCardsDrawnThisTurn = new ArrayList<>();
    this.playedDevelopmentCardThisTurn = false;

    this.remainingSettlements = 5;
    this.remainingCities = 4;
    this.remainingRoads = 15;

    this.possibleRoadEdges = new ArrayList<>();
    this.possibleSettlementNodes = new ArrayList<>();
    this.possibleCityNodes = new ArrayList<>();

    this.roads = new ArrayList<>();

    this.numKnightsPlayed = 0;
    this.largestArmy = false;
    this.longestRoad = false;
    this.longestRoadSize = 0;

    this.rolledDiceThisTurn = false;
    this.hasFinishedTurn = false;
    this.hasDiscardedThisTurn = false;
    this.previousAction = null;

    this.firstStartingSettlement = null;
    this.secondStartingSettlement = null;
  }

  /**
  * Query the player to choose their next action.
  *
  * @return the action the player chose.
  */
  public abstract int[] play(GameState gameState);
  
  //****************************************************************************
  //*************************** Getters and setters ****************************
  //****************************************************************************
  
  //****************************************************************************
  //*********************************** ID *************************************
  //****************************************************************************

  public PlayerID getID() {
    return this.id;
  }
  
  public void setId(PlayerID id) {
    this.id = id;
  }
  
  //****************************************************************************
  //********************************** Cards ***********************************
  //****************************************************************************

  /*---------------------------------Resources--------------------------------*/
  public void addToResourceCards(ResourceCard card) {
    this.resourceCards.put(card, this.resourceCards.get(card) + 1);
  }
  
  public void addToKnownCards(ResourceCard card) {
    this.addToResourceCards(card);
    this.knownCards.put(card, this.knownCards.get(card) + 1);
  }
  
  public void addAmountToResourceCards(ResourceCard card, int amount) {
    this.resourceCards.put(card, this.resourceCards.get(card) + amount);
  }
  
  public void addAmountToKnownCards(ResourceCard card, int amount) {
    this.addAmountToResourceCards(card, amount);
    this.knownCards.put(card, this.knownCards.get(card) + amount);
  }

  /**
  * Removes a random card from this player's hand.
  *
  * @return a random card removed from this player's hand if available; otherwise null.
  */
  public ResourceCard takeRandomCardFromHand() {
    // Get all resource cards which are greater than zero in hand.
    List<ResourceCard> cards = new ArrayList<>();
    for (ResourceCard card : this.resourceCards.keySet()) {
      if (this.resourceCards.get(card) > 0) {
        cards.add(card);
      }
    }

    // Pick one of these resources at random
    if (cards.isEmpty()) {
      return null;
    }
    Random rand = new Random();
    int index = rand.nextInt(cards.size());
    ResourceCard card = cards.get(index);

    // Remove this card from hand.
    this.resourceCards.put(card, this.resourceCards.get(card) - 1);
    return card;
  }
  
  public boolean hasAmountOfResourceInHand(ResourceCard card, int amount) {
    return this.resourceCards.get(card) >= amount;
  }
  
  public int removeAllOfOneCardFromHand(ResourceCard card) {
    if (this.resourceCards.containsKey(card)) {
      int numCards = this.resourceCards.get(card);
      this.resourceCards.put(card, 0);
      return numCards;
    }
    return 0;
  }
  
  /**
   * Removes a certain amount of a resource card from this player's hand.
   *
   * @param card   the resource to remove
   * @param amount amount of resource to remove
   */
  public void removeAmountOfResourceCardFromHand(ResourceCard card, int amount) {
    int resourceSizeAfterRemoval = this.resourceCards.get(card) - amount;
    if (this.resourceCards.get(card) - amount >= 0) {
      this.resourceCards.put(card, resourceSizeAfterRemoval);
    }
  }
  
  /**
   * Returns the total amount of resource cards in this player's hand.
   *
   * @return total amount of resource cards in this player's hand.
   */
  public int getAmountOfResourceCardsInHand() {
    int amount = 0;
    for (ResourceCard card : this.resourceCards.keySet()) {
      amount += this.resourceCards.get(card);
    }
    return amount;
  }
  /*--------------------------------------------------------------------------*/

  /*---------------------------------Dev Cards--------------------------------*/
  public void addDevelopmentCard(DevelopmentCard card) {
    this.developmentCardsDrawnThisTurn.add(card);
  }

  public void removeDevelopmentCard(DevelopmentCard card) {
    this.developmentCards.remove(card);
  }

  public boolean hasPlayedDevelopmentCardThisTurn() {
    return this.playedDevelopmentCardThisTurn;
  }

  public void setPlayedDevelopmentCardThisTurn(boolean playedDevelopmentCardThisTurn) {
    this.playedDevelopmentCardThisTurn = playedDevelopmentCardThisTurn;
  }

  public void addAllDevelopmentCardsDrawnThisTurnToDevelopmentCards() {
    this.developmentCards.addAll(this.developmentCardsDrawnThisTurn);
    this.developmentCardsDrawnThisTurn.clear();
  }
  
  public boolean hasDevelopmentCard(DevelopmentCard card) {
    return this.developmentCards.contains(card);
  }
  /*--------------------------------------------------------------------------*/
  
  //****************************************************************************
  //******************************** Buildings *********************************
  //****************************************************************************

  /*------------------------------- Settlements ------------------------------*/

  public void setRemainingSettlements(int remainingSettlements) {
    this.remainingSettlements = remainingSettlements;
  }
  
  public void addNodeToPossibleSettlementNodes(Node n) {
    this.possibleSettlementNodes.add(n);
  }

  public void removeNodeFromPossibleSettlementNodes(Node n) {
    this.possibleSettlementNodes.remove(n);
  }

  public void setFirstStartingSettlement(Settlement s) {
    this.firstStartingSettlement = s;
  }

  public void setSecondStartingSettlement(Settlement s) {
    this.secondStartingSettlement = s;
  }

  public void clearPossibleSettlementNodes() {
    this.possibleSettlementNodes.clear();
  }
  /*--------------------------------------------------------------------------*/
  
  /*--------------------------------- Cities ---------------------------------*/

  public void setRemainingCities(int remainingCities) {
    this.remainingCities = remainingCities;
  }
  
  public void addNodeToPossibleCityNodes(Node n) {
    this.possibleCityNodes.add(n);
  }

  /*--------------------------------------------------------------------------*/
  
  /*---------------------------------- Roads ---------------------------------*/

  public void setRemainingRoads(int remainingRoads) {
    this.remainingRoads = remainingRoads;
  }
  
  public void addEdgeToPossibleRoadEdges(Edge e) {
    this.possibleRoadEdges.add(e);
  }

  public void removeEdgeFromPossibleRoadEdges(Edge e) {
    this.possibleRoadEdges.remove(e);
  }

  public boolean possibleRoadEdgesDoesNotContain(Edge e) {
    return !this.possibleRoadEdges.contains(e);
  }
  
  public void clearPossibleRoadEdges() {
    this.possibleRoadEdges.clear();
  }
  
  public void addAllEdgesToPossibleRoadEdges(List<Edge> edges) {
    this.possibleRoadEdges.addAll(edges);
  }

  public void addRoad(Road r) {
    this.roads.add(r);
  }

  /*--------------------------------------------------------------------------*/
  
  //****************************************************************************
  //***************************** Victory Points *******************************
  //****************************************************************************

  /*----------------------------- Victory Points -----------------------------*/

  public void setVictoryPoints(int victoryPoints) {
    this.victoryPoints = victoryPoints;
  }
  /*--------------------------------------------------------------------------*/

  /*------------------------------ Largest Army ------------------------------*/
  public boolean hasLargestArmy() {
    return this.largestArmy;
  }

  /*--------------------------------------------------------------------------*/
  
  /*------------------------------ Longest Road ------------------------------*/
  public boolean hasLongestRoad() {
    return this.longestRoad;
  }
  
  public void setLongestRoad(boolean longestRoad) {
    this.longestRoad = longestRoad;
  }

  /*--------------------------------------------------------------------------*/
  
  //****************************************************************************
  //********************************* Metadata *********************************
  //****************************************************************************

  public boolean hasDiscardedThisTurn() {
    return this.hasDiscardedThisTurn;
  }

  public void setHasDiscardedThisTurn(boolean hasDiscardedThisTurn) {
    this.hasDiscardedThisTurn = hasDiscardedThisTurn;
  }

  public void setRolledDiceThisTurn(boolean rolledDiceThisTurn) {
    this.rolledDiceThisTurn = rolledDiceThisTurn;
  }

  public boolean hasRolledDiceThisTurn() {
    return this.rolledDiceThisTurn;
  }

  //****************************************************************************
  //********************************* Overrides ********************************
  //****************************************************************************

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o.getClass() != this.getClass()) {
      return false;
    }
    
    final Player other = (Player) o;
    return this.id == other.id;
  }
}
