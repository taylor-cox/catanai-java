package com.catanai.server.model.bank;

import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.tile.Terrain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO.
 */
public class TestDealer {
  Dealer dealer;
  final List<ResourceCard> resourceCards = Arrays.asList(ResourceCard.values());
  final List<Terrain> terrains = Arrays.asList(Terrain.values());
  final int numDevCards = 25;
  final int numResCards = 19;
  
  @Before
  public void setUp() {
    dealer = new Dealer();
  }
  
  @Test
  public void testDrawDevelopmentCard() {
    Map<DevelopmentCard, Integer> numDevCardsInBank = new HashMap<>();
    for (int i = 0; i < numDevCards; i++) {
      DevelopmentCard devCard = dealer.drawDevelopmentCard();
      Assert.assertNotNull(devCard);
      numDevCardsInBank.merge(devCard, 1, (oldVal, newVal) -> oldVal + newVal);
    }
    Assert.assertEquals(Integer.valueOf(14), numDevCardsInBank.get(DevelopmentCard.KNIGHT));
    Assert.assertEquals(Integer.valueOf(5), numDevCardsInBank.get(DevelopmentCard.VICTORY_POINT));
    Assert.assertEquals(Integer.valueOf(2), numDevCardsInBank.get(DevelopmentCard.YEAR_OF_PLENTY));
    Assert.assertEquals(Integer.valueOf(2), numDevCardsInBank.get(DevelopmentCard.MONOPOLY));
    Assert.assertEquals(Integer.valueOf(2), numDevCardsInBank.get(DevelopmentCard.ROAD_BUILDING));
    Assert.assertNull(dealer.drawDevelopmentCard());
  }
  
  @Test
  public void testDrawReturnResourceCard() {
    // Test that all the resource banks can give 19 resources.
    Map<ResourceCard, Integer> numResourceCards = new HashMap<>();
    for (int i = 0; i < numResCards; i++) {
      for (ResourceCard resCard : resourceCards) {
        ResourceCard drawnResCard = dealer.drawResource(resCard);
        Assert.assertNotNull(drawnResCard);
        numResourceCards.merge(drawnResCard, 1, (oldVal, newVal) -> oldVal + newVal);
      }
    }
    
    // Test all resources are of size 19
    for (ResourceCard resCard : resourceCards) {
      Assert.assertEquals(Integer.valueOf(19), numResourceCards.get(resCard));
    }
    
    // Test that attempting to draw another card results in no card being drawn.
    for (ResourceCard resCard : resourceCards) {
      Assert.assertNull(dealer.drawResource(resCard));
    }
    
    // Test that all banks can return all resource cards until full.
    for (int i = 0; i < numResCards; i++) {
      for (ResourceCard resCard : resourceCards) {
        Assert.assertTrue(dealer.returnResource(resCard));
      }
    }
    
    // Test that attempting to add resource past limit is false.
    for (ResourceCard resCard : resourceCards) {
      Assert.assertFalse(dealer.returnResource(resCard));
    }
    
    // Test drawing resources via terrain interface.
    numResourceCards = new HashMap<>();
    for (int i = 0; i < numResCards; i++) {
      for (Terrain t : terrains) {
        if (t == Terrain.DESERT) {
          continue;
        }
        ResourceCard drawnResCard = dealer.drawResource(t);
        Assert.assertNotNull(drawnResCard);
        numResourceCards.merge(drawnResCard, 1, (oldVal, newVal) -> oldVal + newVal);
      }
    }

    // Test drawing resources from empty bank via terrain interface.
    for (Terrain t : terrains) {
      if (t == Terrain.DESERT) {
        continue;
      }
      Assert.assertNull(dealer.drawResource(t));
    }

    // Test returning all resources to each of the banks via terrain interface.
    for (int i = 0; i < numResCards; i++) {
      for (Terrain t : terrains) {
        if (t == Terrain.DESERT) {
          continue;
        }
        Assert.assertTrue(dealer.returnResource(t));
      }
    }

    // Test attempting to return resource over specified amount being false.
    for (Terrain t : terrains) {
      if (t == Terrain.DESERT) {
        continue;
      }
      Assert.assertFalse(dealer.returnResource(t));
    }

  }
}
