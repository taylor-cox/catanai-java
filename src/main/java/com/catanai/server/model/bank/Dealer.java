package com.catanai.server.model.bank;

import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.tile.Terrain;
import java.util.HashMap;

/**
 * Represents a dealer for all banks in a game of Catan.
 */
public final class Dealer {
  private DevelopmentBank developmentCardBank = new DevelopmentBank();
  private ResourceBank woolBank = new ResourceBank(ResourceCard.WOOL);
  private ResourceBank oreBank = new ResourceBank(ResourceCard.ORE);
  private ResourceBank lumberBank = new ResourceBank(ResourceCard.LUMBER);
  private ResourceBank brickBank = new ResourceBank(ResourceCard.BRICK);
  private ResourceBank grainBank = new ResourceBank(ResourceCard.GRAIN);
  
  public DevelopmentCard drawDevelopmentCard() {
    return developmentCardBank.takeCard();
  }
  
  /**
  * Draws resource from the relevant bank.
  *
  * @param c resource card associated with resource to draw.
  * @return a resource card of the associated terrain.
  */
  public ResourceCard drawResource(ResourceCard c) {
    ResourceBank bank = this.getAssociatedBank(c);
    if (bank == null) {
      return null;
    }
    return bank.takeCard();
  }
  
  /**
  * Draws resource from the relevant bank.
  *
  * @param t terrain associated with resource to draw.
  * @return a resource card of the associated terrain.
  */
  public ResourceCard drawResource(Terrain t) {
    ResourceBank bank = this.getAssociatedBank(t);
    if (bank == null) {
      return null;
    }
    return bank.takeCard();
  }
  
  /**
  * Returns resource to associated bank.
  *
  * @param c card to return
  * @return whether adding the resource back to the bank was successful
  */
  public boolean returnResource(ResourceCard c) {
    ResourceBank bank = this.getAssociatedBank(c);
    return bank.addCard(c);
  }
  
  /**
  * Returns resource to associated bank.
  *
  * @param t terrain associated with resource to return
  * @return whether adding the resource back to the bank was successful
  */
  public boolean returnResource(Terrain t) {
    ResourceBank bank = this.getAssociatedBank(t);
    return bank.addCard(this.getAssociatedResource(t));
  }

  /**
   * Returns the relevant resource bank for the given Terrain, t.
   *
   * @param t terrain which produces a resource
   * @return the relevant bank to the given terrain.
   */
  private ResourceBank getAssociatedBank(Terrain t) {
    switch (t) {
      case FOREST:
        return this.lumberBank;
      case FIELD:
        return this.grainBank;
      case HILL:
        return this.brickBank;
      case MOUNTAIN:
        return this.oreBank;
      case PASTURE:
        return this.woolBank;
      case DESERT:
        return null;
      default:
        return null;
    }
  }

  /**
   * Returns the relevant resource bank for the given ResourceCard, c.
   *
   * @param c resource card associated with bank
   * @return the relevant bank to the given terrain.
   */
  private ResourceBank getAssociatedBank(ResourceCard c) {
    return this.getResourceBanks().get(c);
  }

  /**
   * Returns the resource associated with the terrain t.
   *
   * @param t terrain to get the resource card of.
   * @return the resource card associated with the terrain.
   */
  private ResourceCard getAssociatedResource(Terrain t) {
    switch (t) {
      case FOREST:
        return ResourceCard.LUMBER;
      case FIELD:
        return ResourceCard.GRAIN;
      case HILL:
        return ResourceCard.BRICK;
      case MOUNTAIN:
        return ResourceCard.ORE;
      case PASTURE:
        return ResourceCard.WOOL;
      case DESERT:
        return null;
      default:
        return null;
    }
  }

  /**
   * Whether an amount of the resource is able to be drawn from the resource bank.
   *
   * @param t   terrain which produces resource
   * @param num amount of cards to draw from resource bank
   * @return if the number of resources can be drawn from the resource bank
   */
  public boolean canDrawResource(Terrain t, int num) {
    ResourceBank bank = this.getAssociatedBank(t);
    if (bank == null) {
      return false;
    }
    return bank.getCurrentBankSize() >= num;
  }

  /**
   * Whether an amount of the resource is able to be drawn from the resource bank.
   *
   * @param c   resource attempting to draw
   * @param num amount of cards to draw from resource bank
   * @return if the number of resources can be drawn from the resource bank
   */
  public boolean canDrawResource(ResourceCard c, int num) {
    ResourceBank bank = this.getAssociatedBank(c);
    if (bank == null) {
      return false;
    }
    return bank.getCurrentBankSize() >= num;
  }
  
  public boolean canDrawDevelopmentCard() {
    return this.developmentCardBank.getCurrentBankSize() > 0;
  }

  //****************************************************************************
  //*************************** Getters and setters ****************************
  //****************************************************************************
  
  public DevelopmentBank getDevelopmentBank() {
    return this.developmentCardBank;
  }
  
  /**
   * Returns a map between the card type of the resource bank, and the resource
   * bank.
   *
   * @return map between resource card and associated bank.
   */
  public HashMap<ResourceCard, ResourceBank> getResourceBanks() {
    HashMap<ResourceCard, ResourceBank> resourceBanks = new HashMap<>();
    resourceBanks.put(ResourceCard.BRICK, this.brickBank);
    resourceBanks.put(ResourceCard.ORE, this.oreBank);
    resourceBanks.put(ResourceCard.GRAIN, this.grainBank);
    resourceBanks.put(ResourceCard.LUMBER, this.lumberBank);
    resourceBanks.put(ResourceCard.WOOL, this.woolBank);
    return resourceBanks;
  }
}
