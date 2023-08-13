package com.catanai.server.model.board.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
* Generates a randomized set of tiles for a catan game.
*/
public final class TileGenerator {
  private final int numTiles = 19;
  
  /**
  * Get a list of randomized tiles (terrain and chit values).
  *
  * @return list of randomized tiles.
  */
  public ArrayList<Tile> getRandomizedTiles() {
    ArrayList<Tile> tiles = new ArrayList<Tile>(numTiles);
    ArrayList<Terrain> terrains = this.getShuffledTerrainList();
    ArrayList<TerrainChit> terrainChits = this.getTerrainChitList();
    
    assert terrainChits.size() == terrains.size() - 1 : 
      "Terrains does not match terrains chit size. Cannot instantiate.";
    
    // 50% chance to reverse the direction of the terrain.
    Random rand = new Random();
    boolean reversed = rand.nextInt(2) == 1;
    
    // Reverse direction of terrain if needed.
    if (reversed) {
      terrainChits = this.reverseTerrainChitPlacement(terrainChits);
    }
    
    // Randomly rotate terrain chits around the board.
    terrainChits = this.randomlyRotateTerrainChits(terrainChits);
    
    // Add tiles to list and return.
    for (Terrain t : terrains) {
      if (t == Terrain.DESERT) {
        Tile toAdd = new Tile(t, TerrainChit.NONE);
        tiles.add(toAdd);
        continue;
      }
      Tile toAdd = new Tile(t, terrainChits.get(0));
      terrainChits.remove(0);
      tiles.add(toAdd);
    }
    
    // Sanity check.
    assert tiles.size() == numTiles : 
      "The number of tiles does not match how many should be on a Catan board.";
    
    return tiles;
  }
  
  /**
  * Randomly rotates the given terrain chits hexagonally.
  *
  * @param terrainChits terrain chit list to randomly rotate
  * @return list of randomly rotated terrain chits.
  */
  private ArrayList<TerrainChit> randomlyRotateTerrainChits(ArrayList<TerrainChit> terrainChits) {
    ArrayList<TerrainChit> randomlyRotatedChits = new ArrayList<TerrainChit>();
    
    // Generate lists representing "outer" and "inner" edges of board.
    List<TerrainChit> outerEdge = terrainChits.subList(0, 12);
    List<TerrainChit> innerEdge = terrainChits.subList(12, 18);
    
    // Get a random rotation, between 0-5
    Random rand = new Random();
    int rotation = rand.nextInt(6);
    
    // Rotate according to the random rotation.
    Collections.rotate(outerEdge, rotation * 2);
    Collections.rotate(innerEdge, rotation);
    
    for (TerrainChit rotated : outerEdge) {
      randomlyRotatedChits.add(rotated);
    }
    for (TerrainChit rotated : innerEdge) {
      randomlyRotatedChits.add(rotated);
    }
    randomlyRotatedChits.add(terrainChits.get(17));
    
    return randomlyRotatedChits;
  }
  
  /**
  * Returns a hexagonally reversed list of terrain chits.
  *
  * @param terrainChits the terrain chits to reverse.
  * @return hexagonally reversed list of terrain chits.
  */
  private ArrayList<TerrainChit> reverseTerrainChitPlacement(ArrayList<TerrainChit> terrainChits) {
    ArrayList<TerrainChit> reversedChits = new ArrayList<TerrainChit>();
    List<TerrainChit> toReverseOuterEdge = terrainChits.subList(1, 12);
    Collections.reverse(toReverseOuterEdge);
    List<TerrainChit> toReverseInnerEdge = terrainChits.subList(14, 18);
    Collections.reverse(toReverseInnerEdge);
    
    reversedChits.add(terrainChits.get(0));
    for (TerrainChit reversed : toReverseOuterEdge) {
      reversedChits.add(reversed);
    }
    reversedChits.add(terrainChits.get(13));
    for (TerrainChit reversed : toReverseInnerEdge) {
      reversedChits.add(reversed);
    }
    reversedChits.add(terrainChits.get(17));
    
    return reversedChits;
  }
  
  /**
  * Gives a shuffled list of all the terrains on a board.
  *
  * @return shuffled list of all terrains on a board.
  */
  private ArrayList<Terrain> getShuffledTerrainList() {
    ArrayList<Terrain> terrains = new ArrayList<Terrain>();
    
    // Add four of forest, field and pasture
    for (int i = 0; i < 4; i++) {
      terrains.add(Terrain.FOREST);
      terrains.add(Terrain.FIELD);
      terrains.add(Terrain.PASTURE);
    }
    
    // Add 3 of hills and mountains
    for (int i = 0; i < 3; i++) {
      terrains.add(Terrain.HILL);
      terrains.add(Terrain.MOUNTAIN);
    }
    
    // Add one desert
    terrains.add(Terrain.DESERT);
    
    // Shuffle the terrains
    Collections.shuffle(terrains);
    
    return terrains;
  }
  
  /**
  * Gives a list of the terrain chits in the correct order to place on terrains.
  *
  * @return ordered list of terrain chits.
  */
  private ArrayList<TerrainChit> getTerrainChitList() {
    ArrayList<TerrainChit> terrainChits = new ArrayList<TerrainChit>();
    
    // Add terrain chits in order, starting from one corner.
    terrainChits.add(TerrainChit.FIVE);
    terrainChits.add(TerrainChit.TWO);
    terrainChits.add(TerrainChit.SIX);
    terrainChits.add(TerrainChit.THREE);
    terrainChits.add(TerrainChit.EIGHT);
    terrainChits.add(TerrainChit.TEN);
    terrainChits.add(TerrainChit.NINE);
    terrainChits.add(TerrainChit.TWELVE);
    terrainChits.add(TerrainChit.ELEVEN);
    terrainChits.add(TerrainChit.FOUR);
    terrainChits.add(TerrainChit.EIGHT);
    terrainChits.add(TerrainChit.TEN);
    terrainChits.add(TerrainChit.NINE);
    terrainChits.add(TerrainChit.FOUR);
    terrainChits.add(TerrainChit.FIVE);
    terrainChits.add(TerrainChit.SIX);
    terrainChits.add(TerrainChit.THREE);
    terrainChits.add(TerrainChit.ELEVEN);
    
    return terrainChits;
  }
}
