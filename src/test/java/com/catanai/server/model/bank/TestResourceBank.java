package com.catanai.server.model.bank;

import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.bank.card.ResourceCard;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * TODO.
 */
public class TestResourceBank {
  final List<ResourceCard> cards = Arrays.asList(ResourceCard.values());
  static final int sizeOfResourceBank = 19;
  
  @Test
  public void testResourceBankFunctionality() {
    for (ResourceCard resCard : cards) {
      ResourceBank resBank = new ResourceBank(resCard);
      for (int i = 0; i < sizeOfResourceBank; i++) {
        ResourceCard drawnCard = resBank.takeCard();
        Assert.assertEquals(resCard, drawnCard);
        Assert.assertNotNull(drawnCard);
      }
      Assert.assertNull(resBank.takeCard());
      for (int i = 0; i < sizeOfResourceBank; i++) {
        Assert.assertTrue(resBank.addCard(resCard));
      }
      Assert.assertFalse(resBank.addCard(resCard));
      Assert.assertFalse(resBank.addCard(DevelopmentCard.KNIGHT));
    }
  }
}
