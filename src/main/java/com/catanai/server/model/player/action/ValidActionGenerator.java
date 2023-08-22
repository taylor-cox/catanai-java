package com.catanai.server.model.player.action;


import ai.djl.modality.rl.ActionSpace;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import com.catanai.server.model.Game;
import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.building.City;
import com.catanai.server.model.board.building.Road;
import com.catanai.server.model.board.building.Settlement;
import com.catanai.server.model.board.graph.Edge;
import com.catanai.server.model.board.graph.Node;
import com.catanai.server.model.player.Player;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.NotImplementedException;


/**
 * Generates all the current valid moves for a player in a game.
 */
public final class ValidActionGenerator {
  Game game;

  public ValidActionGenerator(Game game) {
    this.game = game;
  }

  /**
   * Gets all the valid actions for the current player, p.
   *
   * @param p current player
   * @return actionspace containing all the valid actions for the current player.
   */
  public ActionSpace getValidActions(Player p) {
    ActionSpace actionSpace = new ActionSpace();
    for (Action a : Action.values()) {
      List<NDList> validActions = this.getValidActionsForPlayer(a, p);
      for (NDList validAction : validActions) {
        actionSpace.add(validAction);
      }
    }
    return actionSpace;
  }

  private List<NDList> getValidActionsForPlayer(Action a, Player p) {
    List<NDList> validActions = new ArrayList<NDList>();
    switch (a) {
      case ACCEPT_TRADE:
        validActions.addAll(this.generateAllValidAcceptTradeActions(p));
        break;
      case DECLINE_TRADE:
        validActions.addAll(this.generateAllValidDeclineTradeActions(p));
        break;
      case DISCARD:
        validActions.addAll(this.generateAllValidDiscardActions(p));
        break;
      case DRAW_DEVELOPMENT_CARD:
        validActions.addAll(this.generateAllValidDrawDevelopmentCardActions(p));
        break;
      case END_TURN:
        validActions.addAll(this.generateAllValidEndTurnActions(p));
        break;
      case MOVE_ROBBER:
        validActions.addAll(this.generateAllValidMoveRobberActions(p));
        break;
      case OFFER_TRADE:
        validActions.addAll(this.generateAllValidOfferTradeActions(p));
        break;
      case PLAY_CITY:
        validActions.addAll(this.generateAllValidPlaceCityActions(p));
        break;
      case PLAY_KNIGHT:
        validActions.addAll(this.generateAllValidPlayKnightActions(p));
        break;
      case PLAY_MONOPOLY:
        validActions.addAll(this.generateAllValidPlayMonopolyActions(p));
        break;
      case PLAY_ROAD:
        validActions.addAll(this.generateAllValidPlayRoadActions(p));
        break;
      case PLAY_ROAD_BUILDING:
        validActions.addAll(this.generateAllValidPlayRoadBuildingActions(p));
        break;
      case PLAY_SETTLEMENT:
        validActions.addAll(this.generateAllValidPlaySettlementActions(p));
        break;
      case PLAY_YEAR_OF_PLENTY:
        validActions.addAll(this.generateAllValidYearOfPlentyActions(p));
        break;
      default:
        break;
    }
    return validActions;
  }

  private Collection<? extends NDList> generateAllValidYearOfPlentyActions(Player p) {
    List<NDList> possibleActions = new ArrayList<NDList>();

    if (!p.getDevelopmentCards().contains(DevelopmentCard.YEAR_OF_PLENTY)) {
      return possibleActions;
    }

    for (ResourceCard rc1 : ResourceCard.values()) {
      for (ResourceCard rc2 : ResourceCard.values()) {
        NDList action = new NDList();
        try (NDManager ndManager = NDManager.newBaseManager()) {
          NDArray actionMetadata = ndManager.zeros(new Shape(11), DataType.INT8);
          actionMetadata.set(new NDIndex(0), Action.PLAY_YEAR_OF_PLENTY.getValue());
          actionMetadata.set(new NDIndex(1), rc1.getValue());
          actionMetadata.set(new NDIndex(2), rc2.getValue());
          action.add(actionMetadata);
        }
        possibleActions.add(action);
      }
    }
    
    return possibleActions;
  }

  private Collection<? extends NDList> generateAllValidPlaySettlementActions(Player p) {
    List<NDList> possibleActions = new ArrayList<NDList>();

    List<ResourceCard> settlementRequiredResources = new ArrayList<ResourceCard>(4);
    settlementRequiredResources.add(ResourceCard.BRICK);
    settlementRequiredResources.add(ResourceCard.GRAIN);
    settlementRequiredResources.add(ResourceCard.WOOL);
    settlementRequiredResources.add(ResourceCard.LUMBER);
    if (!p.getResourceCards().containsAll(settlementRequiredResources) && !this.game.startingTurnSettlement()) {
      return possibleActions;
    }

    for (int i = 0; i < 54; i++) {
      if (!this.game.getBoard().canPlaceSettlement(new Settlement(i, p.getId(), this.game.startingTurnSettlement()))) {
        continue;
      }
      NDList action = new NDList();
      try (NDManager ndManager = NDManager.newBaseManager()) {
        NDArray actionMetadata = ndManager.zeros(new Shape(11), DataType.INT8);
        actionMetadata.set(new NDIndex(0), Action.PLAY_SETTLEMENT.getValue());
        actionMetadata.set(new NDIndex(1), i);
        action.add(actionMetadata);
      }
      possibleActions.add(action);
    }
    return possibleActions;
  }

  private Collection<? extends NDList> generateAllValidPlayRoadBuildingActions(Player p) {
    // TODO: THIS FUNCTION IS THE BEST THING EVER!!!!!!!
    throw new NotImplementedException("TODO: not implemented yet.");
    List<NDList> possibleActions = new ArrayList<NDList>();

    if (!p.getDevelopmentCards().contains(DevelopmentCard.ROAD_BUILDING)) {
      return possibleActions;
    }

    List<Node> playerNodesWithBuilding = this.game.getBoard().getNodes().stream()
        .filter((node) -> node.hasBuilding() && node.getBuilding().getPlayerId() == p.getId())
        .collect(Collectors.toList());
    
    List<Edge> playerEdgesWithRoads = this.game.getBoard().getEdges().stream()
        .filter((edge) -> edge.hasRoad() && edge.getRoad().getPlayerId() == p.getId())
        .collect(Collectors.toList());

    for (int i = 0; i < 54; i++) {
      if (!this.game.getBoard().canPlaceSettlement(new Settlement(i, p.getId(), this.game.startingTurnRoad()))) {
        continue;
      }
      NDList action = new NDList();
      try (NDManager ndManager = NDManager.newBaseManager()) {
        NDArray actionMetadata = ndManager.zeros(new Shape(11), DataType.INT8);
        actionMetadata.set(new NDIndex(0), Action.PLAY_SETTLEMENT.getValue());
        actionMetadata.set(new NDIndex(1), i);
        action.add(actionMetadata);
      }
      possibleActions.add(action);
    }
    return possibleActions;
  }

  private Collection<? extends NDList> generateAllValidPlayRoadActions(Player p) {
    List<NDList> possibleActions = new ArrayList<NDList>();

    List<ResourceCard> settlementRequiredResources = new ArrayList<ResourceCard>(2);
    settlementRequiredResources.add(ResourceCard.BRICK);
    settlementRequiredResources.add(ResourceCard.LUMBER);
    if (!p.getResourceCards().containsAll(settlementRequiredResources) && !this.game.startingTurnRoad()) {
      return possibleActions;
    }

    for (int i = 0; i < 72; i++) {
      if (!this.game.getBoard().canPlaceRoad(new Road(i, p.getId(), this.game.startingTurnRoad()))) {
        continue;
      }
      NDList action = new NDList();
      try (NDManager ndManager = NDManager.newBaseManager()) {
        NDArray actionMetadata = ndManager.zeros(new Shape(11), DataType.INT8);
        actionMetadata.set(new NDIndex(0), Action.PLAY_ROAD.getValue());
        actionMetadata.set(new NDIndex(1), i);
        action.add(actionMetadata);
      }
      possibleActions.add(action);
    }
    return possibleActions;
  }

  private Collection<? extends NDList> generateAllValidPlayMonopolyActions(Player p) {
    List<NDList> possibleActions = new ArrayList<NDList>();

    if (!p.getDevelopmentCards().contains(DevelopmentCard.MONOPOLY)) {
      return possibleActions;
    }

    for (ResourceCard rc : ResourceCard.values()) {
      NDList action = new NDList();
      try (NDManager ndManager = NDManager.newBaseManager()) {
        NDArray actionMetadata = ndManager.zeros(new Shape(11), DataType.INT8);
        actionMetadata.set(new NDIndex(0), Action.PLAY_MONOPOLY.getValue());
        actionMetadata.set(new NDIndex(1), rc.getValue());
        action.add(actionMetadata);
      }
      possibleActions.add(action);
    }
    return possibleActions;
  }

  private Collection<? extends NDList> generateAllValidPlayKnightActions(Player p) {
    List<NDList> possibleActions = new ArrayList<NDList>();

    if (!p.getDevelopmentCards().contains(DevelopmentCard.KNIGHT)) {
      return possibleActions;
    }

    for (int i = 0; i < 19; i++) {
      NDList action = new NDList();
      try (NDManager ndManager = NDManager.newBaseManager()) {
        NDArray actionMetadata = ndManager.zeros(new Shape(11), DataType.INT8);
        actionMetadata.set(new NDIndex(0), Action.PLAY_KNIGHT.getValue());
        actionMetadata.set(new NDIndex(1), i);
        action.add(actionMetadata);
      }
      possibleActions.add(action);
    }
    return possibleActions;
  }

  private Collection<? extends NDList> generateAllValidPlaceCityActions(Player p) {
    List<NDList> possibleActions = new ArrayList<NDList>();

    List<ResourceCard> cityRequiredResources = new ArrayList<ResourceCard>(5);
    cityRequiredResources.add(ResourceCard.ORE);
    cityRequiredResources.add(ResourceCard.ORE);
    cityRequiredResources.add(ResourceCard.ORE);
    cityRequiredResources.add(ResourceCard.GRAIN);
    cityRequiredResources.add(ResourceCard.GRAIN);
    if (!p.getResourceCards().containsAll(cityRequiredResources)) {
      return possibleActions;
    }

    List<Node> currentPlayerBuildingNodes = this.game.getBoard().getNodes().stream()
        .filter((node) -> node.hasBuilding() && node.getBuilding().getPlayerId() == p.getId())
        .collect(Collectors.toList());

    for (Node node : currentPlayerBuildingNodes) {
      if (!this.game.getBoard().canPlaceCity(new City(node.getIndex(), p.getId()))) {
        continue;
      }
      NDList action = new NDList();
      try (NDManager ndManager = NDManager.newBaseManager()) {
        NDArray actionMetadata = ndManager.zeros(new Shape(11), DataType.INT8);
        actionMetadata.set(new NDIndex(0), Action.PLAY_SETTLEMENT.getValue());
        actionMetadata.set(new NDIndex(1), node.getIndex());
        action.add(actionMetadata);
      }
      possibleActions.add(action);
    }
    return possibleActions;
  }

  private Collection<? extends NDList> generateAllValidOfferTradeActions(Player p) {
    throw new NotImplementedException("TODO: not implemented yet.");
    List<NDList> possibleActions = new ArrayList<NDList>();

    if (!(p.getResourceCards().size() > 0)) {
      return possibleActions;
    }

    HashMap<ResourceCard, Integer> cardAmounts = new HashMap<ResourceCard, Integer>();

    for (ResourceCard r : p.getResourceCards()) {
      cardAmounts.merge(r, 1, (prevVal, curVal) -> prevVal + curVal);
    }

    for (int i = 0; i < cardAmounts.get(ResourceCard.BRICK); i++) {

    }
    return possibleActions;
  }

  private Collection<? extends NDList> generateAllValidMoveRobberActions(Player p) {
    List<NDList> possibleActions = new ArrayList<NDList>();

    if (this.game.getLastDiceRollValue() != 7) {
      return possibleActions;
    }

    for (int i = 0; i < 19; i++) {
      NDList action = new NDList();
      try (NDManager ndManager = NDManager.newBaseManager()) {
        if (this.game.getBoard().getTileIndexCurrentlyBlocked() == i) {
          continue;
        }
        NDArray actionMetadata = ndManager.zeros(new Shape(11), DataType.INT8);
        actionMetadata.set(new NDIndex(0), Action.MOVE_ROBBER.getValue());
        actionMetadata.set(new NDIndex(1), i);
        action.add(actionMetadata);
      }
      possibleActions.add(action);
    }
    return possibleActions;
  }

  private Collection<? extends NDList> generateAllValidEndTurnActions(Player p) {
    List<NDList> possibleActions = new ArrayList<NDList>();

    if (this.game.getLastDiceRollValue() != 7) {
      return possibleActions;
    }

    NDList action = new NDList();
    try (NDManager ndManager = NDManager.newBaseManager()) {
      NDArray actionMetadata = ndManager.zeros(new Shape(11), DataType.INT8);
      actionMetadata.set(new NDIndex(0), Action.END_TURN.getValue());
      action.add(actionMetadata);
    }
    possibleActions.add(action);

    return possibleActions;
  }

  private Collection<? extends NDList> generateAllValidDrawDevelopmentCardActions(Player p) {
    List<NDList> possibleActions = new ArrayList<NDList>();

    List<ResourceCard> settlementRequiredResources = new ArrayList<ResourceCard>(4);
    settlementRequiredResources.add(ResourceCard.ORE);
    settlementRequiredResources.add(ResourceCard.GRAIN);
    settlementRequiredResources.add(ResourceCard.WOOL);
    if (!p.getResourceCards().containsAll(settlementRequiredResources)) {
      return possibleActions;
    }

    NDList action = new NDList();
    try (NDManager ndManager = NDManager.newBaseManager()) {
      NDArray actionMetadata = ndManager.zeros(new Shape(11), DataType.INT8);
      actionMetadata.set(new NDIndex(0), Action.DRAW_DEVELOPMENT_CARD.getValue());
      action.add(actionMetadata);
    }
    possibleActions.add(action);

    return possibleActions;
  }

  private Collection<? extends NDList> generateAllValidDiscardActions(Player p) {
    List<NDList> subsets = new ArrayList<NDList>();

    if (!(p.getResourceCards().size() > 7) || !(this.game.getLastDiceRollValue() == 7)) {
      return subsets;
    }

    for (int i = 0; i < 54; i++) {
      if (!this.game.getBoard().canPlaceSettlement(new Settlement(i, p.getId(), this.game.startingTurnSettlement()))) {
        continue;
      }
      NDList action = new NDList();
      try (NDManager ndManager = NDManager.newBaseManager()) {
        NDArray actionMetadata = ndManager.zeros(new Shape(11), DataType.INT8);
        actionMetadata.set(new NDIndex(0), Action.PLAY_SETTLEMENT.getValue());
        actionMetadata.set(new NDIndex(1), i);
        action.add(actionMetadata);
      }
      subsets.add(action);
    }
    return subsets;
  }

  private List<List<Integer>> findCombinations(ArrayList<Integer> nums, int target) {
      List<List<Integer>> result = new ArrayList<>();
      backtrack(nums, target, 0, new ArrayList<>(), result);
      return result;
  }

  private void backtrack(ArrayList<Integer> nums, int target, int index, List<Integer> currentCombination, List<List<Integer>> result) {
      if (target == 0) {
          result.add(new ArrayList<>(currentCombination));
          return;
      }

      for (int i = index; i < nums.size(); i++) {
          if (target - nums.get(i) >= 0) {
              currentCombination.add(nums.get(i));
              backtrack(nums, target - nums.get(i), i, currentCombination, result);
              currentCombination.remove(currentCombination.size() - 1);
          }
      }
  }

  private Collection<? extends NDList> generateAllValidDeclineTradeActions(Player p) {
    throw new NotImplementedException("TODO: not implemented yet.");
  }

  private Collection<? extends NDList> generateAllValidAcceptTradeActions(Player p) {
    throw new NotImplementedException("TODO: not implemented yet.");
  }
}
