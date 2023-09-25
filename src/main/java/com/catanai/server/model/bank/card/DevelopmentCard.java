package com.catanai.server.model.bank.card;

/**
* Represents a development card.
*/
public enum DevelopmentCard implements Card {
  KNIGHT(0),
  ROAD_BUILDING(1),
  YEAR_OF_PLENTY(2),
  MONOPOLY(3),
  VICTORY_POINT(4);
  
  private final int value;
  
  DevelopmentCard(int value) {
    this.value = value;
  }
  
  public int getValue() {
    return this.value;
  }
}
