package com.catanai.server.model.bank.card;

import java.util.Arrays;
import java.util.Optional;

/**
* Represents a resource card.
*/
public enum ResourceCard implements Card {
  LUMBER(1),
  WOOL(2),
  GRAIN(3),
  BRICK(4),
  ORE(5);
  
  private final int value;

  private ResourceCard(int value) {
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
  public static Optional<ResourceCard> valueOf(int value) {
    return Arrays.stream(values())
    .filter(resource -> resource.value == value)
    .findFirst();
  }
}
