package com.catanai.server.model.board.tile;

/**
* Represents what dice roll the terrain produces on (i.e. the 
* production circles placed at the begining of the game).
* 2-12
*/
public enum TerrainChit {
  TWO(2),
  THREE(3),
  FOUR(4),
  FIVE(5),
  SIX(6),
  EIGHT(8),
  NINE(9),
  TEN(10),
  ELEVEN(11),
  TWELVE(12),
  NONE(0);
  
  private final int id;
  
  /**
  * Creates a terrain chit with the id @param id.
  *
  * @param id id of terrain chit.
  */
  TerrainChit(int id) {
    this.id = id;
  }
  
  public int getValue() {
    return id;
  }
}
