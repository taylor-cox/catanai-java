package com.catanai.server.model.board.tile;

/**
* Represents a port on the edge of the game board.
*/
public enum Port {
  THREE_TO_ONE(1),
  GRAIN_TWO_TO_ONE(2),
  ORE_TWO_TO_ONE(3),
  WOOL_TWO_TO_ONE(4),
  BRICK_TWO_TO_ONE(5),
  LUMBER_TWO_TO_ONE(6),
  NO_PORT(0);
  
  private final int value;

  /**
   * Creates a new port with the value @param value.
   *
   * @param value value of the port.
   */
  private Port(int value) {
    this.value = value;
  }
  
  public int getValue() {
    return this.value;
  }
}
