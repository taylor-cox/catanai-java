package com.catanai.server.model.board.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
* Generates a randomized set of tiles for a catan game.
*/
public final class TileGenerator {
  private static final int numTiles = 19;
  
  /**
  * Get a list of randomized tiles (terrain and chit values).
  *
  * @return list of randomized tiles.
  */
  public List<Tile> getRandomizedTiles() {
    List<Tile> tiles = new ArrayList<Tile>(numTiles);
    for (int i = 0; i < numTiles; i++) {
      tiles.add(null);
    }
    List<Terrain> terrains = this.getShuffledTerrainList();
    List<TerrainChit> terrainChits = this.getTerrainChitList();

    // Initialize the indexes of the inner ring, outer ring and middle.
    List<Integer> outerRing = Arrays.asList(new Integer[] { 0, 1, 2, 6, 11, 15, 18, 17, 16, 12, 7, 3 });
    List<Integer> innerRing = Arrays.asList(new Integer[] { 4, 5, 10, 14, 13, 8 });
    int middle = 9;

    // Check where to start terrain chit placement.
    Random rand = new Random();
    int start = rand.nextInt(6);
    System.out.println(start);
    // Rotate the inner and outer rings to the correct starting position.
    Collections.rotate(outerRing, start * 2);
    Collections.rotate(innerRing, start);


    // 50% chance to reverse the direction of the terrain chits.
    if (rand.nextInt(2) == 1) {
      for (int i = 1; i < outerRing.size() / 2; i++) {
        int temp = outerRing.get(i);
        outerRing.set(i, outerRing.get(outerRing.size() - i));
        outerRing.set(outerRing.size() - i, temp);
      }
      for (int i = 1; i < innerRing.size() / 2; i++) {
        int temp = innerRing.get(i);
        innerRing.set(i, innerRing.get(innerRing.size() - i));
        innerRing.set(innerRing.size() - i, temp);
      }
    }

    // Handle outer ring.
    for (Integer index : outerRing) {
      Terrain attemptingToAdd = terrains.remove(0);
      if (attemptingToAdd == Terrain.DESERT) {
        tiles.set(index, new Tile(Terrain.DESERT, TerrainChit.NONE));
      } else {
        tiles.set(index, new Tile(attemptingToAdd, terrainChits.remove(0)));
      }
    }

    // Handle inner ring.
    for (Integer index : innerRing) {
      Terrain attemptingToAdd = terrains.remove(0);
      if (attemptingToAdd == Terrain.DESERT) {
        tiles.set(index, new Tile(Terrain.DESERT, TerrainChit.NONE));
      } else {
        tiles.set(index, new Tile(attemptingToAdd, terrainChits.remove(0)));
      }
    }

    // Handle middle.
    Terrain attemptingToAdd = terrains.remove(0);
    if (attemptingToAdd == Terrain.DESERT) {
      tiles.set(middle, new Tile(Terrain.DESERT, TerrainChit.NONE));
    } else {
      tiles.set(middle, new Tile(attemptingToAdd, terrainChits.remove(0)));
    }
    
    // Sanity check.
    assert tiles.size() == numTiles : 
      "The number of tiles does not match how many should be on a Catan board.";
    
    return tiles;
  }
  
  /**
  * Gives a shuffled list of all the terrains on a board.
  *
  * @return shuffled list of all terrains on a board.
  */
  private List<Terrain> getShuffledTerrainList() {
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
  private List<TerrainChit> getTerrainChitList() {
    ArrayList<TerrainChit> terrainChits = new ArrayList<TerrainChit>();
    
    // Add terrain chits in order, starting from one corner.
    terrainChits.add(TerrainChit.FIVE);
    terrainChits.add(TerrainChit.EIGHT);
    terrainChits.add(TerrainChit.FOUR);
    terrainChits.add(TerrainChit.ELEVEN);
    terrainChits.add(TerrainChit.TWELVE);
    terrainChits.add(TerrainChit.NINE);
    terrainChits.add(TerrainChit.TEN);
    terrainChits.add(TerrainChit.EIGHT);
    terrainChits.add(TerrainChit.THREE);
    terrainChits.add(TerrainChit.SIX);
    terrainChits.add(TerrainChit.TWO);
    terrainChits.add(TerrainChit.TEN);
    terrainChits.add(TerrainChit.THREE);
    terrainChits.add(TerrainChit.SIX);
    terrainChits.add(TerrainChit.FIVE);
    terrainChits.add(TerrainChit.FOUR);
    terrainChits.add(TerrainChit.NINE);
    terrainChits.add(TerrainChit.ELEVEN);
    
    return terrainChits;
  }
}
