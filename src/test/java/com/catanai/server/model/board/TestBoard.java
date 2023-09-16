package com.catanai.server.model.board;

import com.catanai.server.model.board.building.City;
import com.catanai.server.model.board.building.Road;
import com.catanai.server.model.board.building.Settlement;
import com.catanai.server.model.board.tile.Tile;
import com.catanai.server.model.player.PlayerID;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the Board class, and all member methods thoroughly.
 */
public class TestBoard {
  Board board;

  @Before
  public void setUp() {
    board = new Board();
  }

  @Test
  public void testPlaceInitialSettlement() {
    // Place a default starting settlement
    Settlement firstSettlement = new Settlement(0, PlayerID.ONE, true);
    Assert.assertTrue(board.placeSettlement(firstSettlement));

    Assert.assertTrue(board.getNodes().get(0).hasBuilding());
    Assert.assertTrue(board.getNodes().get(0).getBuilding().getPlayerId() == PlayerID.ONE);
    Assert.assertTrue(board.getNodes().get(0).getBuilding().getPlacement() == 0);
  }

  @Test
  public void testPlaceSettlementAfterTwoRoads() {
    // Place a default starting settlement
    Settlement firstSettlement = new Settlement(0, PlayerID.ONE, true);
    Assert.assertTrue(board.placeSettlement(firstSettlement));

    // Place two roads extending from settlement
    Road firstRoad = new Road(1, PlayerID.ONE);
    Assert.assertTrue(board.placeRoad(firstRoad));
    Road secondRoad = new Road(7, PlayerID.ONE);
    Assert.assertTrue(board.placeRoad(secondRoad));

    // Attempt to place second settlement connecting to two roads.
    Settlement secondSettlement = new Settlement(8, PlayerID.ONE, false);
    Assert.assertTrue(board.placeSettlement(secondSettlement));

    // Check nodes have settlements
    Assert.assertTrue(board.getNodes().get(8).hasBuilding());
    Assert.assertTrue(board.getNodes().get(8).getBuilding().getPlayerId() == PlayerID.ONE);
    Assert.assertTrue(board.getNodes().get(8).getBuilding().getPlacement() == 8);
  }

  @Test
  public void testCannotPlaceSettlementNearAnotherSettlement() {
    // Place a default starting settlement for P1
    Settlement firstSettlement = new Settlement(0, PlayerID.ONE, true);
    Assert.assertTrue(board.placeSettlement(firstSettlement));
    Assert.assertNotNull(board.getNodes().get(0).getBuilding());

    // Attempt placing a settlement near first settlement
    Settlement secondSettlement = new Settlement(4, PlayerID.ONE, true);
    Assert.assertFalse(board.placeSettlement(secondSettlement));
    Assert.assertNull(board.getNodes().get(4).getBuilding());

    // Attempt placing a settlement near first settlement
    Settlement thirdSettlement = new Settlement(3, PlayerID.TWO, true);
    Assert.assertFalse(board.placeSettlement(thirdSettlement));
    Assert.assertNull(board.getNodes().get(3).getBuilding());

    // Attempt building to a settlement spot which cannot be placed on by
    // another player.
    Settlement fourthSettlement = new Settlement(12, PlayerID.TWO, true);
    Road road1 = new Road(11, PlayerID.TWO);
    Road road2 = new Road(6, PlayerID.TWO);
    Settlement fifthSettlement = new Settlement(3, PlayerID.TWO, false);

    Assert.assertTrue(board.placeSettlement(fourthSettlement));
    Assert.assertTrue(board.placeRoad(road1));
    Assert.assertTrue(board.placeRoad(road2));
    Assert.assertFalse(board.placeSettlement(fifthSettlement));

    Assert.assertNotNull(board.getNodes().get(12).getBuilding());
    Assert.assertNotNull(board.getEdges().get(11).getRoad());
    Assert.assertNotNull(board.getEdges().get(6).getRoad());
    Assert.assertNull(board.getNodes().get(3).getBuilding());
  }
  
  @Test
  public void testRoadSettlementsAtIntersection() {
    // Attempt to place three settlements at intersection.
    Settlement settlement1 = new Settlement(13, PlayerID.ONE, true);
    Settlement settlement2 = new Settlement(23, PlayerID.ONE, false);
    Settlement settlement3 = new Settlement(24, PlayerID.ONE, false);
    Road road1 = new Road(20, PlayerID.ONE);
    Road road2 = new Road(27, PlayerID.ONE);
    Road road3 = new Road(28, PlayerID.ONE);

    // First settlement
    Assert.assertTrue(board.placeSettlement(settlement1));
    Assert.assertFalse(board.placeSettlement(settlement2));
    Assert.assertFalse(board.placeSettlement(settlement3));

    // First road
    Assert.assertFalse(board.placeRoad(road2));
    Assert.assertFalse(board.placeRoad(road3));
    Assert.assertTrue(board.placeRoad(road1));

    // Second settlement
    Assert.assertTrue(board.placeRoad(road2));
    Assert.assertTrue(board.placeSettlement(settlement2));

    // Third settlement
    Assert.assertTrue(board.placeRoad(road3));
    Assert.assertTrue(board.placeSettlement(settlement3));

    // Sanity check that nodes and edges have building
    Assert.assertTrue(board.getNodes().get(13).hasBuilding());
    Assert.assertTrue(board.getNodes().get(23).hasBuilding());
    Assert.assertTrue(board.getNodes().get(24).hasBuilding());
    Assert.assertTrue(board.getEdges().get(20).hasRoad());
    Assert.assertTrue(board.getEdges().get(27).hasRoad());
    Assert.assertTrue(board.getEdges().get(28).hasRoad());
  }

  @Test
  public void testPlaceCityOnOwnedSettlement() {
    // Settlement and city placement
    Settlement toPlaceSettlement = new Settlement(0, PlayerID.ONE, true);
    City toPlaceCity = new City(0, PlayerID.ONE);
    
    // Checks for placing city on settlement
    Assert.assertFalse(board.placeCity(toPlaceCity));
    Assert.assertTrue(board.placeSettlement(toPlaceSettlement));
    Assert.assertTrue(board.getNodes().get(0).hasBuilding() 
        && board.getNodes().get(0).getBuilding() instanceof Settlement);
    Assert.assertTrue(board.placeCity(toPlaceCity));
    Assert.assertTrue(board.getNodes().get(0).hasBuilding() 
        && board.getNodes().get(0).getBuilding() instanceof City);
  }

  @Test
  public void testPlaceRoadNearSettlement() {
    // Create the settlement and test that all nodes of that settlement are placeable.
    Settlement settlement1 = new Settlement(0, PlayerID.ONE, true);
    Road road1 = new Road(0, PlayerID.ONE);
    Road road2 = new Road(1, PlayerID.ONE);

    Assert.assertTrue(board.placeSettlement(settlement1));
    Assert.assertTrue(board.placeRoad(road1));
    Assert.assertTrue(board.placeRoad(road2));

    // Test at edges of board.
    Settlement settlement2 = new Settlement(50, PlayerID.ONE, true);
    Road road3 = new Road(65, PlayerID.ONE);
    Road road4 = new Road(71, PlayerID.ONE);

    Assert.assertTrue(board.placeSettlement(settlement2));
    Assert.assertTrue(board.placeRoad(road3));
    Assert.assertTrue(board.placeRoad(road4));

    // Test placing a road of another player isn't possible
    Settlement settlement3 = new Settlement(41, PlayerID.ONE, true);
    Road road5 = new Road(59, PlayerID.TWO);
    Road road6 = new Road(59, PlayerID.ONE);

    Assert.assertTrue(board.placeSettlement(settlement3));
    Assert.assertFalse(board.placeRoad(road5));
    Assert.assertTrue(board.placeRoad(road6));
  }

  @Test
  public void testPlaceRoadNearRoad() {
    // Test placing a road near a road.
    Settlement settlement1 = new Settlement(0, PlayerID.ONE, true);
    Road road1 = new Road(0, PlayerID.ONE);
    Road road2 = new Road(6, PlayerID.ONE);

    Assert.assertFalse(board.placeRoad(road1));
    Assert.assertFalse(board.placeRoad(road2));
    Assert.assertTrue(board.placeSettlement(settlement1));
    Assert.assertFalse(board.placeRoad(road2));
    Assert.assertTrue(board.placeRoad(road1));
    Assert.assertTrue(board.placeRoad(road2));

    // Test at edge of board.
    Settlement settlement2 = new Settlement(50, PlayerID.ONE, true);
    Road road3 = new Road(71, PlayerID.ONE);
    Road road4 = new Road(70, PlayerID.ONE);

    Assert.assertFalse(board.placeRoad(road3));
    Assert.assertFalse(board.placeRoad(road4));
    Assert.assertTrue(board.placeSettlement(settlement2));
    Assert.assertFalse(board.placeRoad(road4));
    Assert.assertTrue(board.placeRoad(road3));
    Assert.assertTrue(board.placeRoad(road4));

    // Test other player cannot place near road of another player.
    Settlement settlement3 = new Settlement(41, PlayerID.ONE, true);
    Road road5 = new Road(59, PlayerID.TWO);
    Road road6 = new Road(59, PlayerID.ONE);
    Road road7 = new Road(58, PlayerID.TWO);
    Road road8 = new Road(58, PlayerID.ONE);

    Assert.assertTrue(board.placeSettlement(settlement3));
    Assert.assertFalse(board.placeRoad(road5));
    Assert.assertFalse(board.placeRoad(road7));
    Assert.assertTrue(board.placeRoad(road6));
    Assert.assertFalse(board.placeRoad(road5));
    Assert.assertFalse(board.placeRoad(road7));
    Assert.assertTrue(board.placeRoad(road8));
  }
  
  @Test
  public void testPlaceRobber() {
    int robberIndex = board.getTileIndexCurrentlyBlocked();
    Assert.assertTrue(board.getTiles().get(robberIndex).isBlocked());
    Assert.assertFalse(board.placeRobber(robberIndex));
    List<Tile> tiles = board.getTiles();
    for (int i = 0; i < tiles.size(); i++) {
      if (i == robberIndex) {
        continue;
      }
      robberIndex = i;
      Assert.assertTrue(board.placeRobber(i));
      Assert.assertTrue(board.getTiles().get(i).isBlocked());
    }
  }
}
