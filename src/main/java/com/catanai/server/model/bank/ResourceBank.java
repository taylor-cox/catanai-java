package com.catanai.server.model.bank;

import com.catanai.server.model.bank.card.Card;
import com.catanai.server.model.bank.card.ResourceCard;

/**
* Represents a bank containing a resource card.
*/
public final class ResourceBank extends Bank {
  ResourceCard[] cards;
  
  /**
   * Represents a bank of resource type.
   *
   * @param type resource bank represents
   */
  public ResourceBank(ResourceCard type) {
    this.max = 19;
    this.min = 0;
    this.currentCardIndex = this.max - 1;
    this.cards = new ResourceCard[this.max];
    for (int i = 0; i < this.max; i++) {
      this.cards[i] = type;
    }
  }
  
  @Override
  public ResourceCard takeCard() {
    if (this.currentCardIndex < this.min) {
      return null;
    }
    ResourceCard toReturn = this.cards[this.currentCardIndex];
    this.cards[this.currentCardIndex] = null;
    this.currentCardIndex -= 1;
    return toReturn;
  }
  
  @Override
  public boolean addCard(Card card) {
    if (this.currentCardIndex + 1 >= this.max) {
      return false;
    }
    if (!(card instanceof ResourceCard)) {
      return false;
    }
    ResourceCard toAdd = (ResourceCard) card;
    this.currentCardIndex += 1;
    this.cards[this.currentCardIndex] = toAdd;
    return true;
  }
}
