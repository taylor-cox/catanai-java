package com.catanai.server.model.bank;

import com.catanai.server.model.bank.card.Card;
import com.catanai.server.model.bank.card.DevelopmentCard;
import java.util.ArrayList;
import java.util.Collections;

/**
* Represents a bank of development cards.
*/
public final class DevelopmentBank extends Bank {
  DevelopmentCard[] cards;
  
  /**
   * Create a bank of development cards.
   */
  public DevelopmentBank() {
    this.min = 0;
    this.max = 25;
    this.currentCardIndex = this.max - 1;
    this.cards = new DevelopmentCard[this.max];
    ArrayList<DevelopmentCard> cards = new ArrayList<>();
    for (int i = 0; i < 14; i++) {
      cards.add(DevelopmentCard.KNIGHT);
    }
    for (int i = 0; i < 5; i++) {
      cards.add(DevelopmentCard.VICTORY_POINT);
    }
    for (int i = 0; i < 2; i++) {
      cards.add(DevelopmentCard.YEAR_OF_PLENTY);
    }
    for (int i = 0; i < 2; i++) {
      cards.add(DevelopmentCard.MONOPOLY);
    }
    for (int i = 0; i < 2; i++) {
      cards.add(DevelopmentCard.ROAD_BUILDING);
    }
    Collections.shuffle(cards);
    cards.toArray(this.cards);
  }
  
  @Override
  public boolean addCard(Card type) throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
    "Cannot add a card to a development bank."
    );
  }
  
  @Override
  public DevelopmentCard takeCard() {
    if (this.currentCardIndex < this.min) {
      return null;
    }
    DevelopmentCard devCard = this.cards[this.currentCardIndex];
    this.cards[this.currentCardIndex] = null;
    this.currentCardIndex -= 1;
    return devCard;
  }
}
