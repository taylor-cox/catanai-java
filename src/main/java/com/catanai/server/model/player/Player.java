package com.catanai.server.model.player;

import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.gamestate.GameState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
* Represents a Catan player.
*/
public abstract class Player {
  protected PlayerId id;
  protected List<ResourceCard> resourceCards;
  protected List<DevelopmentCard> developmentCards;
  protected int remainingSettlements;
  protected int remainingCities;
  protected int remainingRoads;
  protected boolean largestArmy;
  protected boolean longestRoad;
  protected int victoryPoints;
  protected List<ResourceCard> knownCards;
  protected boolean hasFinishedTurn;
  protected int numKnightsPlayed;
  protected ArrayList<DevelopmentCard> developmentCardsDrawnThisTurn;
  
  /**
   * Initializes all player variables.
   */
  public Player(PlayerId id) {
    this.id = id;
    this.resourceCards = new ArrayList<ResourceCard>();
    this.developmentCards = new ArrayList<DevelopmentCard>();
    this.developmentCardsDrawnThisTurn = new ArrayList<DevelopmentCard>();
    this.remainingSettlements = 5;
    this.remainingCities = 4;
    this.remainingRoads = 15;
    this.largestArmy = false;
    this.longestRoad = false;
    this.victoryPoints = 0;
    this.knownCards = new ArrayList<ResourceCard>();
    this.hasFinishedTurn = false;
    this.numKnightsPlayed = 0;
  }

  /**
  * Query the player to choose their next action.
  *
  * @return the action the player chose.
  */
  public abstract int[] play(GameState gameState);

  /**
   * Reset class to basic values.
   */
  public void reset() {
    this.resourceCards = new ArrayList<ResourceCard>();
    this.developmentCards = new ArrayList<DevelopmentCard>();
    this.remainingSettlements = 5;
    this.remainingCities = 4;
    this.remainingRoads = 15;
    this.largestArmy = false;
    this.longestRoad = false;
    this.victoryPoints = 0;
    this.knownCards = new ArrayList<ResourceCard>();
    this.hasFinishedTurn = false;
    this.numKnightsPlayed = 0;
  }
  
  /**
  * Takes all the cards of type rc from the player's hand.
  *
  * @param rc resource to take from player's hand
  * @return all cards of type taken from the player's hand
  */
  public ArrayList<ResourceCard> takeAllCardsFromHand(ResourceCard rc) {
    ArrayList<ResourceCard> toTake = new ArrayList<ResourceCard>();
    int amountOfCard = (int) this.resourceCards
        .stream()
        .filter((resource) -> { 
          return resource.getValue() == rc.getValue(); 
        })
        .count();
    for (int i = 0; i < amountOfCard; i++) {
      toTake.add(rc);
      this.resourceCards.remove(rc);
    }
    return toTake;
  }
  
  /**
  * Takes all resources from the player's hand and returns them.
  *
  * @param resources resources to take from hand
  * @return the resources taken from the hand
  */
  public List<ResourceCard> takeAllCardsFromHand(List<ResourceCard> resources) {
    ArrayList<ResourceCard> toReturn = new ArrayList<ResourceCard>();
    for (ResourceCard rc : resources) {
      toReturn.add(this.resourceCards.remove(this.resourceCards.indexOf(rc)));
    }
    
    return toReturn;
  }
  
  /**
  * Removes a random card from this player's hand.
  *
  * @return a random card removed from this player's hand if available;
  *     otherwise null.
  */
  public ResourceCard takeRandomCardFromHand() {
    if (this.resourceCards.size() == 0) {
      return null;
    }
    Random rand = new Random();
    int cardToRemove = rand.nextInt(this.resourceCards.size());
    return this.resourceCards.remove(cardToRemove);
  }
  
  //****************************************************************************
  //*************************** Getters and setters ****************************
  //****************************************************************************
  
  public PlayerId getId() {
    return this.id;
  }
  
  public void addToResourceCards(ResourceCard card) {
    this.resourceCards.add(card);
  }
  
  public void addToKnownCards(ResourceCard card) {
    this.resourceCards.add(card);
    this.knownCards.add(card);
  }
  
  /**
   * Adds all the cards from @param cards to known cards.
   *
   * @param cards cards to add to known cards.
   */
  public void addAllToKnownCards(ArrayList<ResourceCard> cards) {
    for (ResourceCard c : cards) {
      this.knownCards.add(c);
      this.resourceCards.add(c);
    }
  }
  
  public List<ResourceCard> getResourceCards() {
    return this.resourceCards;
  }
  
  public int getRemainingSettlements() {
    return this.remainingSettlements;
  }
  
  public void setRemainingSettlements(int remainingSettlements) {
    this.remainingSettlements = remainingSettlements;
  }
  
  public int getRemainingCities() {
    return this.remainingCities;
  }
  
  public void setRemainingCities(int remainingCities) {
    this.remainingCities = remainingCities;
  }
  
  public int getRemainingRoads() {
    return this.remainingRoads;
  }
  
  public void setRemainingRoads(int remainingRoads) {
    this.remainingRoads = remainingRoads;
  }
  
  public boolean hasLargestArmy() {
    return this.largestArmy;
  }
  
  public void setLargestArmy(boolean largestArmy) {
    this.largestArmy = largestArmy;
  }
  
  public boolean hasLongestRoad() {
    return this.longestRoad;
  }
  
  public void setLongestRoad(boolean longestRoad) {
    this.longestRoad = longestRoad;
  }
  
  public int getVictoryPoints() {
    return this.victoryPoints;
  }
  
  public void setVictoryPoints(int victoryPoints) {
    this.victoryPoints = victoryPoints;
  }
  
  public List<ResourceCard> getKnownCards() {
    return this.knownCards;
  }
  
  /**
   * Removes the resource card @param c from hand.
   *
   * @param c resource card to remove from hand
   * @return resource card removed from hand
   */
  public ResourceCard removeResourceCardFromHand(ResourceCard c) {
    if (this.resourceCards.remove(c)) {
      return c;
    } else {
      return null;
    }
  }
  
  public void addDevelopmentCard(DevelopmentCard card) {
    this.developmentCardsDrawnThisTurn.add(card);
  }
  
  public List<DevelopmentCard> getDevelopmentCards() {
    return this.developmentCards;
  }
  
  public boolean hasFinishedTurn() {
    return this.hasFinishedTurn;
  }
  
  /**
   * Sets that the player has finished their turn, and adds all development
   * cards drawn this turn to the list of possibly playable ones.
   *
   * @param hasFinishedTurn what to set hasFinishedTurn to
   */
  public void setHasFinishedTurn(boolean hasFinishedTurn) {
    this.hasFinishedTurn = hasFinishedTurn;
    if (hasFinishedTurn) {
      this.developmentCards.addAll(this.developmentCardsDrawnThisTurn);
      this.developmentCardsDrawnThisTurn.clear();
    }
  }
  
  public int getNumKnightsPlayed() {
    return this.numKnightsPlayed;
  }
  
  public void setNumKnightsPlayed(int numKnightsPlayed) {
    this.numKnightsPlayed = numKnightsPlayed;
  }
  
  /**
   * Remove the development card @param card from hand.
   *
   * @param card development card to remove from hand.
   * @return development card which was removed from hand.
   */
  public DevelopmentCard removeDevelopmentCard(DevelopmentCard card) {
    if (!this.developmentCards.remove(card)) {
      return null;
    }
    return card;
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
