package com.catanai.server.model.board.tile;

/**
* Represents a possible tile terrain.
* Resourced produced on these terrains include:
*  - Wool
*  - Lumber
*  - Brick
*  - Ore
*  - Wheat
*/
public enum Terrain {
  FOREST(1), // Produces lumber
  PASTURE(2), // Produces wool
  FIELD(3), // Produces wheat
  HILL(4), // Produces brick
  MOUNTAIN(5), // Produces ore
  DESERT(0); // Produces nothing
  
  private final int value;
  
  /**
   * Creates a terrain with the given @param id.
   *
   * @param id id of the terrain.
   */
  private Terrain(int id) {
    this.value = id;
  }
  
  public int getValue() {
    return this.value;
  }
}
