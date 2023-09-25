package com.catanai.server.model.bank.card;

import java.util.Arrays;
import java.util.Optional;

/**
* Represents a resource card.
*/
public enum ResourceCard implements Card {
  WOOL(0),
  GRAIN(1),
  LUMBER(2),
  ORE(3),
  BRICK(4);
  
  private final int value;

  ResourceCard(int value) {
    this.value = value;
  }
  
  public int getValue() {
    return value;
  }
  
  /**
   * Returns the ResourceCard associated with the value @param value.
   *
   * @param value value associated with resource card
   * @return a resource card if value is associated with res card; else null.
   */
  public static ResourceCard valueOf(int value) {
    if (value < 0 || value > 4) {
      return null;
    }
    Optional<ResourceCard> rc = Arrays.stream(values())
            .filter(resource -> resource.value == value)
            .findFirst();
    
    return rc.orElse(null);
  }
}
