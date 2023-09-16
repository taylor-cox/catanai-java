package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.board.building.Road;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Class which validates and executes play road building action in game of Catan.
 */
public class RoadBuildingExecutor implements SpecificActionExecutor {
  private final Game game;

  public RoadBuildingExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.BUSINESS_AS_USUAL) {
      return this.playRoadBuilding(amd, p);
    } else {
      return false;
    }
  }

  private boolean playRoadBuilding(ActionMetadata amd, Player p) {
    int road1Index = amd.getRelevantMetadata()[0];
    int road2Index = amd.getRelevantMetadata()[1];

    // Validate
    if (p.hasPlayedDevelopmentCardThisTurn()) {
      return false;
    } else if (!p.hasDevelopmentCard(DevelopmentCard.ROAD_BUILDING)) {
      return false;
    } else if (p.getRemainingRoads() < 2) {
      return false;
    }

    Road road1 = new Road(road1Index, p.getID());
    Road road2 = new Road(road2Index, p.getID());

    if (!this.game.getBoard().canPlaceRoad(road1) || !this.game.getBoard().canPlaceRoad(road2)) {
      return false;
    }

    // Remove road building card from player's hand, remove 2 roads from player,
    // set has played dev card this turn.
    // TODO: Check if roads are connected in line, and determine if the player has longest road.
    p.removeDevelopmentCard(DevelopmentCard.ROAD_BUILDING);
    p.setRemainingRoads(p.getRemainingRoads() - 2);
    p.setPlayedDevelopmentCardThisTurn(true);
    return true;
  }
}
