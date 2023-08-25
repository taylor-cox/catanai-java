package com.catanai.server.model.bank;

import com.catanai.server.model.bank.card.Card;

/**
* Represents a card bank.
*/
public abstract class Bank {
  // protected Card[] cards;
  protected int max;
  protected int min;
  protected int currentCardIndex = max;
  
  /**
  * Takes a card from a bank, and returns it.
  *
  * @return Card card from bank
  * @throws ArrayIndexOutOfBoundsException if bank has no more cards left to give.
  */
  public abstract Card takeCard() throws ArrayIndexOutOfBoundsException;
  
  /** 
  * Returns a card back to the bank.
  *
  * @param card card to return to bank
  * @throws ArrayIndexOutOfBoundsException if adding a card to the bank is impossible.
  */
  public abstract boolean addCard(Card card);
  
  /**
  * Returns the amount of cards in the bank.
  *
  * @return the current size of bank
  */
  public int getCurrentBankSize() {
    return currentCardIndex + 1;
  }
}
