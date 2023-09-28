package com.catanai.server.model.bank.card;

import java.util.Arrays;
import java.util.Optional;

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

  public static DevelopmentCard valueOf(int value) {
    if (value < 0 || value > 4) {
      return null;
    }
    Optional<DevelopmentCard> rc = Arrays.stream(values())
            .filter(resource -> resource.value == value)
            .findFirst();
    
    return rc.orElse(null);
  }
}
