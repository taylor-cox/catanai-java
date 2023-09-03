package com.catanai.server.model.player.action;

import com.catanai.server.model.Game;
import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.building.Building;
import com.catanai.server.model.board.building.City;
import com.catanai.server.model.board.building.Road;
import com.catanai.server.model.board.building.Settlement;
import com.catanai.server.model.board.graph.Edge;
import com.catanai.server.model.board.graph.Node;
import com.catanai.server.model.board.tile.Terrain;
import com.catanai.server.model.board.tile.Tile;
import com.catanai.server.model.gamestate.GameState;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.PlayerId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
* Class which executes the actions of a player on a game of Catan.
*/
public final class ActionExecutor {
  private Game game;
  private final HashMap<Action, ResourceCard[]> requiredResourcesForAction;
  private final ResourceCard[] orderOfCards = {
    ResourceCard.WOOL,
    ResourceCard.GRAIN,
    ResourceCard.LUMBER,
    ResourceCard.ORE,
    ResourceCard.BRICK,
  };

  private int lastActionID;
  
  /**
   * Generate an ActionExecutor working on a game @param game.
   *
   * @param game game to execute actions on.
   */
  public ActionExecutor(Game game) {
    this.game = game;
    this.requiredResourcesForAction = new HashMap<Action, ResourceCard[]>();
    this.initializeRequiredResourcesForAction();
    this.lastActionID = 0;
  }
  
  /**
  * Does the action on the board if possible.
  *
  * @param amd the metadata pertaining to the action
  * @return whether the action was successful or not.
  */
  public boolean doAction(ActionMetadata amd, Player p) {
    this.lastActionID = amd.getAction().getValue();
    System.out.println("Player: " + p.getId().getValue());
    System.out.println(amd.getAction());
    boolean successful = false;
    switch (amd.getAction()) {
      // Handle building actions
      case PLAY_SETTLEMENT:
        successful = this.playSettlementHelper(amd, p);
        break;
      case PLAY_CITY:
        successful = this.playCityHelper(amd, p);
        break;
      case PLAY_ROAD:
        successful = this.playRoadHelper(amd, p);
        break;
      // Handle development card actions
      case PLAY_KNIGHT:
        successful = this.playKnightHelper(amd, p);
        break;
      case PLAY_ROAD_BUILDING:
        successful = this.playRoadBuildingHelper(amd, p);
        break;
      case PLAY_MONOPOLY:
        successful = this.playMonopolyHelper(amd, p);
        break;
      case PLAY_YEAR_OF_PLENTY:
        successful = this.playYearOfPlentyHelper(amd, p);
        break;
      // Draw development card.
      case DRAW_DEVELOPMENT_CARD:
        successful = this.drawDevelopmentCardHelper(amd, p);
        break;
      // Trading
      case OFFER_TRADE:
        successful = this.offerTradeHelper(amd, p);
        break;
      case ACCEPT_TRADE:
        successful = this.acceptTradeHelper(amd, p);
        break;
      case DECLINE_TRADE:
        successful = this.declineTradeHelper(amd, p);
        break;
      // Move robber
      case MOVE_ROBBER:
        successful = this.moveRobberHelper(amd, p);
        break;
      // Discard cards
      case DISCARD:
        successful = this.discardHelper(amd, p);
        break;
      // End turn
      case END_TURN:
        successful = true;
        break;
      default:
        successful = false;
        break;
    }
    return successful;
  }
  
  /**
  * Private class for returning a settlement and a gamestate from second
  * settlement action in starting turns.
  */
  public class SettlementGameStatePair {
    private Settlement settlement;
    private GameState gameState;
    
    public SettlementGameStatePair(Settlement settlement, GameState gameState) {
      this.settlement = settlement;
      this.gameState = gameState;
    }
    
    public GameState getGameState() {
      return this.gameState;
    }
    
    public Settlement getSettlement() {
      return this.settlement;
    }
  }
  
  /**************************************************************************/
  /********************** Starting Turns Helper Functions *******************/
  /**************************************************************************/

  /**
  * Queries player until a valid settlement location is chosen on the board.
  *
  * @param p player settling
  * @return gamestate after the starting settlement is placed.
  */
  public GameState startingSettlement(Player p, GameState gs) {
    int[] actionMetadata = p.play(gs);
    ActionMetadata amd = new ActionMetadata(actionMetadata);
    Action curAction = amd.getAction();
    this.lastActionID = curAction.getValue();
    
    if (curAction != Action.PLAY_SETTLEMENT || amd.getRelevantMetadata()[0] > 53) {
      return null;
    }

    Settlement attemptedSettlement = new Settlement(
        amd.getRelevantMetadata()[0],
        p.getId(),
        true
    );

    if (!this.game.getBoard().placeSettlement(attemptedSettlement)) {
      return null;
    } else {
      p.setRemainingSettlements(p.getRemainingSettlements() - 1);
      p.setVictoryPoints(p.getVictoryPoints() + 1);
      return new GameState(this.game);
    }
  }

  /**
   * Queries player until a valid road location is chosen on the board.
   *
   * @param p player building road
   * @return gamestate after the road is placed.
   */
  public GameState startingRoad(Player p, GameState gs) {
    int[] actionMetadata = p.play(gs);
    ActionMetadata amd = new ActionMetadata(actionMetadata);
    Action curAction = amd.getAction();
    this.lastActionID = curAction.getValue();

    if (curAction != Action.PLAY_ROAD || amd.getRelevantMetadata()[0] > 71) {
      return null;
    }

    Road attemptedRoad = new Road(
        amd.getRelevantMetadata()[0],
        p.getId()
    );

    if (!this.game.getBoard().placeRoad(attemptedRoad)) {
      return null;
    }
    p.setRemainingRoads(p.getRemainingRoads() - 1);
    return new GameState(this.game);
  }

  /**
   * Queries player until a valid second settlement location is chosen on the
   * board,
   * and adds cards to hand from connected terrain tiles.
   *
   * @param p player settling
   * @return settlement and gamestate after second settlement is placed.
   */
  public SettlementGameStatePair secondSettlement(Player p, GameState gs) {
    int[] actionMetadata = p.play(gs);
    ActionMetadata amd = new ActionMetadata(actionMetadata);
    Action curAction = amd.getAction();
    this.lastActionID = curAction.getValue();

    if (curAction != Action.PLAY_SETTLEMENT || amd.getRelevantMetadata()[0] > 53) {
      return null;
    }

    Settlement attemptedSettlement = new Settlement(
        amd.getRelevantMetadata()[0],
        p.getId(),
        true);

    if (!this.game.getBoard().placeSettlement(attemptedSettlement)) {
      return null;
    } else {
      ArrayList<Terrain> attemptedSettlementTerrains = this.game.getBoard()
          .getNodes()
          .get(attemptedSettlement.getPlacement())
          .getProduces();
      for (Terrain t : attemptedSettlementTerrains) {
        if (this.game.getDealer().canDrawResource(t, 1)) {
          p.addToKnownCards(this.game.getDealer().drawResource(t));
        }
      }
      p.setRemainingSettlements(p.getRemainingSettlements() - 1);
      p.setVictoryPoints(p.getVictoryPoints() + 1);
      return new SettlementGameStatePair(
          attemptedSettlement,
          new GameState(this.game));
    }
  }

  /**
   * Queries player until a valid second road location is chosen on the board.
   * Makes sure the second road is placed next to the second settlement.
   *
   * @param p player placing road
   * @param secondSettlement player's second settlement
   * @return gamestate after second road is placed.
   */
  public GameState secondRoad(Player p, Settlement secondSettlement, GameState gs) {
    int[] actionMetadata = p.play(gs);
    ActionMetadata amd = new ActionMetadata(actionMetadata);
    Action curAction = amd.getAction();
    this.lastActionID = curAction.getValue();

    if (curAction != Action.PLAY_ROAD || amd.getRelevantMetadata()[0] > 71) {
      return null;
    }

    Road attemptedRoad = new Road(
        amd.getRelevantMetadata()[0],
        p.getId());

    boolean roadNearSecondSettlement = false;
    ArrayList<Edge> toCheck = this.game
        .getBoard()
        .getNodes()
        .get(secondSettlement.getPlacement())
        .getConnectedEdges();
    for (Edge e : toCheck) {
      roadNearSecondSettlement |= e.getIndex() == attemptedRoad.getPlacement();
    }

    if (!roadNearSecondSettlement || !this.game.getBoard().placeRoad(attemptedRoad)) {
      return null;
    } else {
      p.setRemainingRoads(p.getRemainingRoads() - 1);
      return new GameState(this.game);
    }
  }

  /**
   * Adds required resources for specific actions to the
   * requiredResourcesForAction
   * map.
   */
  private void initializeRequiredResourcesForAction() {
    // Add city requirements
    ResourceCard[] cityRequirements = new ResourceCard[] {
        ResourceCard.ORE,
        ResourceCard.ORE,
        ResourceCard.ORE,
        ResourceCard.GRAIN,
        ResourceCard.GRAIN
    };
    this.requiredResourcesForAction.put(Action.PLAY_CITY, cityRequirements);

    // Add settlement requirements
    ResourceCard[] settlementRequirements = new ResourceCard[] {
        ResourceCard.BRICK,
        ResourceCard.WOOL,
        ResourceCard.GRAIN,
        ResourceCard.LUMBER
    };
    this.requiredResourcesForAction.put(Action.PLAY_SETTLEMENT, settlementRequirements);

    // Add road requirements
    ResourceCard[] roadRequirements = new ResourceCard[] {
        ResourceCard.BRICK,
        ResourceCard.LUMBER
    };
    this.requiredResourcesForAction.put(Action.PLAY_ROAD, roadRequirements);

    // Add development card requirements
    ResourceCard[] devCardRequirements = new ResourceCard[] {
        ResourceCard.ORE,
        ResourceCard.GRAIN,
        ResourceCard.WOOL
    };
    this.requiredResourcesForAction.put(Action.DRAW_DEVELOPMENT_CARD, devCardRequirements);
  }

  /**************************************************************************/
  /************************* doAction Helper Functions **********************/
  /**************************************************************************/
  /**
   * Checks requirements for playing a settlement, and returns whether
   * requirements were met and settlement was successfully played.
   *
   * @param amd metadata pertaining to the settlement placement
   * @param p   player attemping to place settlement.
   * @return whether settlement was able to be placed
   */
  private boolean playSettlementHelper(ActionMetadata amd, Player p) {
    // Ensure player has all the required resources for a settlement
    ResourceCard[] requiredResources = this.requiredResourcesForAction.get(amd.getAction());
    if (!p.getResourceCards().containsAll(Arrays.asList(requiredResources))) {
      return false;
    }

    List<ResourceCard> takenCards = p.takeAllCardsFromHand(Arrays.asList(requiredResources));

    if (!takenCards.containsAll(Arrays.asList(requiredResources))) {
      return false;
    }

    Settlement s = new Settlement(
        amd.getRelevantMetadata()[0],
        p.getId(),
        false);

    if (!this.game.getBoard().placeSettlement(s)) {
      return false;
    } else {
      p.setRemainingSettlements(p.getRemainingSettlements() - 1);
      p.setVictoryPoints(p.getVictoryPoints() + 1);
      return true;
    }
  }

  /**
   * Checks the requirements for a player playing a city, and returns if
   * playing the city was successful.
   *
   * @param amd metadata pertaining to playing a city action
   * @param p   player attemtping to place city
   * @return whether playing a city was successful or not
   */
  private boolean playCityHelper(ActionMetadata amd, Player p) {
    // Ensure player has all the required resources for a city
    ResourceCard[] requiredResources = this.requiredResourcesForAction.get(amd.getAction());
    if (!p.getResourceCards().containsAll(Arrays.asList(requiredResources))) {
      return false;
    }

    List<ResourceCard> takenCards = p.takeAllCardsFromHand(Arrays.asList(requiredResources));

    if (!takenCards.containsAll(Arrays.asList(requiredResources))) {
      return false;
    }

    City c = new City(
        amd.getRelevantMetadata()[0],
        p.getId());

    if (!this.game.getBoard().placeCity(c)) {
      return false;
    } else {
      p.setRemainingCities(p.getRemainingCities() - 1);
      p.setRemainingSettlements(p.getRemainingSettlements() + 1);
      p.setVictoryPoints(p.getVictoryPoints() + 1);
      return true;
    }
  }

  /**
   * Checks the requirements for a player playing a road, and returns if
   * playing the road was successful.
   *
   * @param amd metadata pertaining to playing a road action
   * @param p   player attempting to place road
   * @return whether placing the road was successful or not.
   */
  private boolean playRoadHelper(ActionMetadata amd, Player p) {
    // Ensure player has all the required resources for a road
    ResourceCard[] requiredResources = this.requiredResourcesForAction.get(amd.getAction());
    if (!p.getResourceCards().containsAll(Arrays.asList(requiredResources))) {
      return false;
    }

    List<ResourceCard> takenCards = p.takeAllCardsFromHand(Arrays.asList(requiredResources));

    if (!takenCards.containsAll(Arrays.asList(requiredResources))) {
      return false;
    }

    Road r = new Road(
        amd.getRelevantMetadata()[0],
        p.getId());
    if (!this.game.getBoard().placeRoad(r)) {
      return false;
    } else {
      p.setRemainingRoads(p.getRemainingRoads() - 1);
      // TODO: Add victory points if longest road.
      return true;
    }
  }

  /**
   * Ensures that the player is able to play a knight, and moves the robber.
   * to the expected location
   *
   * @param amd metadata pertaining to playing a knight
   * @param p   player attempting to play knight
   * @return whether the attempt was successful or not.
   */
  private boolean playKnightHelper(ActionMetadata amd, Player p) {
    if (!p.getDevelopmentCards().contains(DevelopmentCard.KNIGHT)) {
      return false;
    }
    p.removeDevelopmentCard(DevelopmentCard.KNIGHT);
    int tileIndex = amd.getRelevantMetadata()[0];
    if (tileIndex > 18) {
      return false;
    }
    this.game.getBoard().placeRobber(amd.getRelevantMetadata()[0]);
    // TODO: Add victory points if largest army.
    return true;
  }

  /**
   * Checks the requirements for a player playing road building development
   * card, and returns if playing road building was successful.
   *
   * @param amd metadata pertaining to playing road building
   * @param p   player attempting to play road building
   * @return whether playing road building was successful or not
   */
  private boolean playRoadBuildingHelper(ActionMetadata amd, Player p) {
    DevelopmentCard devCard;
    if (!p.getDevelopmentCards().contains(DevelopmentCard.ROAD_BUILDING)) {
      return false;
    }

    devCard = p.removeDevelopmentCard(DevelopmentCard.ROAD_BUILDING);

    if (devCard != DevelopmentCard.ROAD_BUILDING) {
      return false;
    }

    Road road1 = new Road(
        amd.getRelevantMetadata()[0],
        p.getId());
    Road road2 = new Road(
        amd.getRelevantMetadata()[1],
        p.getId());

    if (!this.game.getBoard().canPlaceRoad(road1) 
          || !this.game.getBoard().canPlaceRoad(road2)
          || p.getRemainingRoads() < 2) {
      return false;
    } else {
      this.game.getBoard().placeRoad(road1);
      this.game.getBoard().placeRoad(road2);
      p.setRemainingRoads(p.getRemainingRoads() - 2);
      return true;
    }
  }

  /**
   * Checks the requirements for a player playing monopoly development card,
   * and returns if playing the monopoly was successful.
   *
   * @param amd metadata pertaining to playing a monopoly
   * @param p   player attempting to play monopoly
   * @return whether monopoly was successful or not
   */
  private boolean playMonopolyHelper(ActionMetadata amd, Player p) {
    DevelopmentCard devCard;
    if (!p.getDevelopmentCards().contains(DevelopmentCard.MONOPOLY)) {
      return false;
    }

    devCard = p.removeDevelopmentCard(DevelopmentCard.MONOPOLY);

    if (devCard == DevelopmentCard.MONOPOLY) {
      return false;
    }

    ResourceCard monopolyResource = ResourceCard.valueOf(amd.getRelevantMetadata()[0]).get();
    for (Player player : this.game.getPlayers()) {
      if (player.equals(p)) {
        continue;
      }
      ArrayList<ResourceCard> cards = player.takeAllCardsFromHand(monopolyResource);
      p.addAllToKnownCards(cards);
    }
    return true;
  }

  /**
   * Checks the requirements for a player playing year of plenty development
   * card, and returns if playing the monopoly was successful.
   *
   * @param amd metadata pertaining to playing year of plenty
   * @param p   player attempting to play year of plenty
   * @return whether playing year of plenty was successful or not
   */
  private boolean playYearOfPlentyHelper(ActionMetadata amd, Player p) {
    DevelopmentCard devCard;
    if (!p.getDevelopmentCards().contains(DevelopmentCard.YEAR_OF_PLENTY)) {
      return false;
    }

    devCard = p.removeDevelopmentCard(DevelopmentCard.YEAR_OF_PLENTY);

    if (devCard != DevelopmentCard.YEAR_OF_PLENTY) {
      return false;
    }

    ResourceCard res1;
    ResourceCard res2;
    // TODO: This is jank, fix this.
    if (ResourceCard.valueOf(amd.getRelevantMetadata()[0]).isPresent()) {
      res1 = ResourceCard.valueOf(amd.getRelevantMetadata()[0]).get();
    } else {
      return false;
    }

    if (ResourceCard.valueOf(amd.getRelevantMetadata()[1]).isPresent()) {
      res2 = ResourceCard.valueOf(amd.getRelevantMetadata()[1]).get();
    } else {
      return false;
    }

    if (res1.getValue() == res2.getValue() && !this.game.getDealer().canDrawResource(res1, 2)) {
      return false;
    } else if (res1.getValue() 
        == res2.getValue() && this.game.getDealer().canDrawResource(res1, 2)) {
      res1 = this.game.getDealer().drawResource(res1);
      res2 = this.game.getDealer().drawResource(res2);
      p.addToKnownCards(res1);
      p.addToKnownCards(res2);
      return true;
    }
    if (this.game.getDealer().canDrawResource(res1, 1) 
        && this.game.getDealer().canDrawResource(res2, 1)) {
      res1 = this.game.getDealer().drawResource(res1);
      res2 = this.game.getDealer().drawResource(res2);
      p.addToKnownCards(res1);
      p.addToKnownCards(res2);
      return true;
    }
    return false;
  }

  /**
   * Checks the requirements for a player attempting to draw a development
   * card, and returns if drawing the development card was successful.
   *
   * @param amd metadata pertaining to drawing a development card
   * @param p   player attempting to draw the development card
   * @return whether the drawing of the development card was successful or not
   */
  private boolean drawDevelopmentCardHelper(ActionMetadata amd, Player p) {
    // Ensure player has all the required resources for a development card
    ResourceCard[] requiredResources = this.requiredResourcesForAction.get(amd.getAction());
    if (!p.getResourceCards().containsAll(Arrays.asList(requiredResources))) {
      return false;
    }
    if (!this.game.getDealer().canDrawDevelopmentCard()) {
      return false;
    }

    List<ResourceCard> takenCards = p.takeAllCardsFromHand(Arrays.asList(requiredResources));

    if (!takenCards.containsAll(Arrays.asList(requiredResources))) {
      return false;
    }

    p.addDevelopmentCard(this.game.getDealer().drawDevelopmentCard());
    return true;
  }

  /**
   * Offers a trade to the other players, and receives a response from all the
   * other players on whether they accept or decline the trade.
   *
   * @param amd metadata of offer trading action
   * @param p   player accepting the trade
   * @return TODO
   */
  private boolean offerTradeHelper(ActionMetadata amd, Player p) {
    // TODO implement
    return false;
  }

  /**
   * Accept the current trade by the current player p to the other player.
   *
   * @param amd metadata of accept trade offer
   * @param p   player accepting the trade
   * @return TODO
   */
  private boolean acceptTradeHelper(ActionMetadata amd, Player p) {
    // TODO implement
    return false;
  }

  /**
   * Decline the current trade by the current player p to the other player.
   *
   * @param amd metadata of decline trade offer
   * @param p   player declining the trade
   * @return TODO
   */
  private boolean declineTradeHelper(ActionMetadata amd, Player p) {
    // TODO implement
    return false;
  }

  /**
   * Attempts to move the robber to a new tile by the player, and returns
   * whether the player was able to move the robber or not.
   *
   * @param amd metadata pertaining to moving the robber
   * @param p   player attempting to move the robber
   * @return whether the player successfully moved the robber or not.
   */
  private boolean moveRobberHelper(ActionMetadata amd, Player p) {
    int tileIndex = amd.getRelevantMetadata()[0];
    int playerIndex = amd.getRelevantMetadata()[1];

    // Validity of tile placement check.
    if (tileIndex > 18) {
      return false;
    }
    Tile tileToBlock = this.game.getBoard().getTiles().get(tileIndex);
    if (tileToBlock.isBlocked()) {
      return false;
    }

    // Get the player's settled / citied at the tile placement,
    // and add their
    Set<PlayerId> possiblePlayersToRob = new HashSet<PlayerId>();
    for (Node n : tileToBlock.getNodes()) {
      Building b = n.getBuilding();
      if (b != null) {
        possiblePlayersToRob.add(b.getPlayerId());
      }
    }

    // Checks that player to rob is available.
    if (!possiblePlayersToRob.contains(PlayerId.valueOf(playerIndex))) {
      return false;
    }

    Player toRob = this.game.getPlayers()
        .stream()
        .filter((player) -> player.getId() == PlayerId.valueOf(playerIndex))
        .findFirst()
        .get();

    this.game.getBoard().placeRobber(tileIndex);
    if (toRob != null) {
      p.addToResourceCards(toRob.takeRandomCardFromHand());
    }
    return true;
  }

  /**
   * Checks that the player has the required cards in hand to discard, and then
   * discards them if possible.
   *
   * @param amd metadata for discard
   * @param p   player to discard from hand
   * @return whether the discard was successful or not
   */
  private boolean discardHelper(ActionMetadata amd, Player p) {
    // Current cards in players hand
    List<ResourceCard> currentCards = p.getResourceCards();
    // Sanity check
    if (currentCards.size() <= 7 || this.game.getLastDiceRollValue() != 7 || p.hasDiscardedThisTurn()) {
      return false;
    } else if (true) {
      return false;
    }

    // Amount of each resource in the player's hand
    HashMap<ResourceCard, Integer> amtOfCurrentCards = new HashMap<>();
    for (ResourceCard c : currentCards) {
      amtOfCurrentCards.merge(c, 1, (oldVal, newVal) -> oldVal + newVal);
    }

    // Check to make sure the player has the required amount to discard in hand.
    int[] cardsToDiscard = amd.getRelevantMetadata();
    for (int i = 0; i < cardsToDiscard.length; i++) {
      if (!(amtOfCurrentCards.get(this.orderOfCards[i]) >= cardsToDiscard[i])) {
        return false;
      }
    }

    for (int i = 0; i < cardsToDiscard.length; i++) {
      for (int j = 0; j < cardsToDiscard[i]; j++) {
        if (p.removeResourceCardFromHand(this.orderOfCards[i]) == null) {
          // TODO: Error, there was some error in the discard function.
          return false;
        }
      }
    }

    return false;
  }

  /**
   * Make players with more than 7 cards discard half of their cards.
   */
  public GameState makePlayersDiscard() {
    // TODO: Change this.
    this.game.getPlayers().forEach((player) -> {
      if (player.getResourceCards().size() > 7) {
        this.makePlayerDiscard(player, game.getCurrentGameState());
      }
    });
    return new GameState(this.game);
  }

  /**
   * Ensure that this player discards, and they discard the correct amount.
   *
   * @param p the player required to discard resource cards
   */
  private void makePlayerDiscard(Player p, GameState currentGameState) {
    ActionMetadata amd;
    boolean toContinue;
    do {
      amd = new ActionMetadata(p.play(currentGameState));
      toContinue = amd.getAction() != Action.DISCARD 
          && IntStream.of(amd.getRelevantMetadata()).sum() 
              != Math.floor(p.getResourceCards().size() / 2);
    } while (toContinue);
    if (!doAction(amd, p)) {
      makePlayerDiscard(p, currentGameState);
    }
  }

  /**
   * Ensure that the current player moves the robber to a valid space.
   *
   * @param p player to move robber
   */
  public GameState makePlayerMoveRobber(Player p, GameState currentGameState) {
    ActionMetadata amd;
    boolean notValidAction;
    do {
      amd = new ActionMetadata(p.play(currentGameState));
      notValidAction = amd.getAction() != Action.MOVE_ROBBER;
    } while (notValidAction);
    if (!this.doAction(amd, p)) {
      this.makePlayerMoveRobber(p, currentGameState);
    }
    return new GameState(this.game);
  }

  public int getLastActionID() {
    return this.lastActionID;
  }
}
