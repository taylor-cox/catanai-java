package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.building.City;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Class which validates and executes build city action in game of Catan.
 */
public class CityExecutor implements SpecificActionExecutor {
  private final Game game;

  /**
   * Constructor for CityExecutor.
   *
   * @param game game to execute action on.
   */
  public CityExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.BUSINESS_AS_USUAL) {
      return this.buildCity(amd, p);
    } else {
      return false;
    }
  }

  /**
   * Builds a city for the player if possible; returns true or false depending on whether the action
   * was successful.
   *
   * @param amd metadata pertaining to the action
   * @param p player attempting to build city
   * @return whether the action was successful or not
   */
  public boolean buildCity(ActionMetadata amd, Player p) {
    // Ensure player has all the required resources for a city
    boolean hasOre = p.hasAmountOfResourceInHand(ResourceCard.ORE, 3);
    boolean hasGrain = p.hasAmountOfResourceInHand(ResourceCard.GRAIN, 2);

    // Validate the player can place the city.
    if (!hasOre || !hasGrain) { // Validate player has enough ore and wheat for city.
      return false;
    } else if (amd.getRelevantMetadata()[0] > 53) { // Validate the placement is on the board.
      return false;
    } else if (p.getRemainingCities() < 1) { // Validate the player has cities to place.
      return false;
    } else if (
        !this.game.getBoard()
            .placeCity(
              new City(amd.getRelevantMetadata()[0], p.getID())
        )) { // Validate the city can be placed on the board.
      return false;
    }

    // City placed on board; update player's values to reflect city placement
    // and return true.
    p.setRemainingCities(p.getRemainingCities() - 1);
    p.setRemainingSettlements(p.getRemainingSettlements() + 1);
    p.setVictoryPoints(p.getVictoryPoints() + 1);
    p.removeAmountOfResourceCardFromHand(ResourceCard.ORE, 3);
    p.removeAmountOfResourceCardFromHand(ResourceCard.GRAIN, 2);
    return true;
  }
}
