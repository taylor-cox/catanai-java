package com.catanai.server.model.player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the ID of a Catan player.
 */
public enum PlayerId {
  ONE(1),
  TWO(2),
  THREE(3),
  FOUR(4);
  
  private final int value;

  private PlayerId(int value) {
    this.value = value;
  }
  
  public int getValue() {
    return this.value;
  }
  
  @Override
  public String toString() {
    return Integer.toString(this.value);
  }
  
  /**
   * Get the player id associated with the value @param value.
   *
   * @param value value associated with player id
   * @return player id associated with value; null if no associated player id
   */
  public static PlayerId valueOf(int value) {
    List<PlayerId> possiblePlayerIds = Arrays.stream(values())
        .filter(resource -> resource.value == value)
        .collect(Collectors.toList());
    if (possiblePlayerIds.size() <= 0) {
      return null;
    } else {
      return possiblePlayerIds.get(0);
    }
  }
}
