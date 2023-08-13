package com.catanai.server.model.bank;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import com.catanai.server.model.bank.card.DevelopmentCard;

public class TestDevelopmentBank {
    DevelopmentBank devBank;

    @Before
    public void setUp() {
        devBank = new DevelopmentBank();
    }

    @Test
    public void testAllDevelopmentCardsInDevelopmentBank() {
        final int NUM_DEV_CARDS = 25;
        List<DevelopmentCard> devCards = new ArrayList<>();
        for(int i = 0; i < NUM_DEV_CARDS; i++) {
            DevelopmentCard currentDevCard = devBank.takeCard();
            Assert.assertNotNull(currentDevCard);
            devCards.add(currentDevCard);
        }
        // Check how many knights in development deck
        int numKnights = (int) devCards.stream()
            .filter((devCard) -> devCard.getValue() == DevelopmentCard.KNIGHT.getValue())
            .count();
        Assert.assertEquals(14, numKnights);

        // Check how many victory points in development deck
        int numVictoryPoints = (int) devCards.stream()
            .filter((devCard) -> devCard.getValue() == DevelopmentCard.VICTORY_POINT.getValue())
            .count();
        Assert.assertEquals(5, numVictoryPoints);

        // Check how many road building in development deck
        int numRoadBuilding = (int) devCards.stream()
            .filter((devCard) -> devCard.getValue() == DevelopmentCard.ROAD_BUILDING.getValue())
            .count();
        Assert.assertEquals(2, numRoadBuilding);

        // Check how many year of plenty in development deck
        int numYearOfPlenty = (int) devCards.stream()
            .filter((devCard) -> devCard.getValue() == DevelopmentCard.YEAR_OF_PLENTY.getValue())
            .count();
        Assert.assertEquals(2, numYearOfPlenty);

        // Check how many monopoly in development deck
        int numMonopoly = (int) devCards.stream()
            .filter((devCard) -> devCard.getValue() == DevelopmentCard.MONOPOLY.getValue())
            .count();
        Assert.assertEquals(2, numMonopoly);

        // Ensure attempting to take another development card returns null.
        Assert.assertNull(devBank.takeCard());

        // Ensure the current bank size is 0 after drawing all cards.
        Assert.assertEquals(0, devBank.getCurrentBankSize());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotAddCardToDevelopmentBank() {
        devBank.addCard(null);
    }
}
