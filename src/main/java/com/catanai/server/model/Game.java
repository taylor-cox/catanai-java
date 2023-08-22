package com.catanai.server.model;

import ai.djl.modality.rl.ActionSpace;
import ai.djl.modality.rl.agent.RlAgent;
import ai.djl.modality.rl.env.RlEnv;
import ai.djl.ndarray.NDList;

import com.catanai.server.model.bank.Dealer;
import com.catanai.server.model.board.Board;
import com.catanai.server.model.board.building.Settlement;
import com.catanai.server.model.board.graph.Node;
import com.catanai.server.model.board.tile.Terrain;
import com.catanai.server.model.board.tile.Tile;
import com.catanai.server.model.gamestate.GameState;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.action.Action;
import com.catanai.server.model.player.action.ActionExecutor;
import com.catanai.server.model.player.action.ActionMetadata;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

/**
 * Represents game of Catan.
 */
public final class Game implements RlEnv {
  private List<Player> players;
  private Player currentPlayer;
  private Board board;
  private Dealer dealer;
  private GameState currentGameState;
  private List<GameState> gameStates;
  private boolean ended;
  private int lastDiceRollValue;
  private ActionExecutor actionExecutor;
  private State currentState;
  private Settlement previousSettlement;

  private enum State {
    START_BUILD, START_ROAD, START2_BUILD, START2_ROAD, BUSINESS_AS_USUAL, TRADE
  }

  /**
   * Creates a new Catan game with players @param players.
   *
   * @param players players in a game of Catan.
   */
  public Game(List<Player> players) {
    this.players = players;
    this.currentPlayer = players.get(0);
    this.board = new Board();
    this.dealer = new Dealer();
    this.gameStates = new ArrayList<GameState>();
    this.ended = false;
    this.lastDiceRollValue = 0;
    this.currentGameState = new GameState(this);
    this.gameStates.add(this.currentGameState);
    this.actionExecutor = new ActionExecutor(this);
    this.currentState = State.START_BUILD;
  }

  /**
   * Resets the game to initial state.
   */
  private void resetHelper() {
    for (Player p : this.players) {
      p.reset();
    }
    this.board = new Board();
    this.dealer = new Dealer();
    this.gameStates = new ArrayList<GameState>();
    this.ended = false;
    this.lastDiceRollValue = 0;
    this.currentGameState = new GameState(this);
    this.gameStates.add(this.currentGameState);
    this.actionExecutor = new ActionExecutor(this);
  }

  /**
   * Updates the gamestate, and adds it to the list of gamestates.
   */
  private void updateGamestate() {
    this.currentGameState = new GameState(this);
    this.gameStates.add(this.currentGameState);
  }

  // ****************************************************************************
  // *************************** Gameplay Functions *****************************
  // ****************************************************************************

  /**
   * Play the starting turns of the game, and add them all to gamestates.
   */
  public void startingTurns() {
    for (GameState gs : this.actionExecutor.startingTurns(currentGameState)) {
      this.gameStates.add(gs);
    }
    this.currentGameState = this.gameStates.get(this.gameStates.size() - 1);
  }

  private void makeMove(Player currentPlayer) {
    switch (this.currentState) {
      case START_BUILD:
        handleStartBuild(currentPlayer);
        break;
      case START_ROAD:
        handleStartRoad(currentPlayer);
        break;
      case START2_BUILD:
        handleStartBuild2(currentPlayer);
        break;
      case START2_ROAD:
        handleStartRoad2(currentPlayer);
        break;
      case BUSINESS_AS_USUAL:
        handleTurn(currentPlayer);
        break;
      default:
        return;
    }
  }

  private void handleTurn(Player curPlayer) {
    int diceRoll = this.rollDice();
    if (diceRoll != 7) {
      this.produce(diceRoll);
    } else {
      this.actionExecutor.makePlayersDiscard();
      this.actionExecutor.makePlayerMoveRobber(curPlayer, this.currentGameState);
    }
    ActionMetadata amd = new ActionMetadata(
        curPlayer.play(this.currentGameState));
    while (amd.getAction() != Action.END_TURN) {
      if (this.actionExecutor.doAction(amd, curPlayer)) {
        // TODO: Something with reward function.
      }
      this.updateGamestate();
      amd = new ActionMetadata(curPlayer.play(this.currentGameState));
    }
  }

  /**
   * Handles second starting road of a player.
   *
   * @param currentPlayer current player in game
   */
  private void handleStartRoad2(Player currentPlayer) {
    this.currentGameState = 
        this.actionExecutor.secondRoad(currentPlayer, this.previousSettlement, currentGameState);
    this.gameStates.add(currentGameState);
    if (this.gameStates.size() >= 16) {
      this.currentState = State.BUSINESS_AS_USUAL;
    } else {
      this.currentState = State.START2_BUILD;
      this.currentPlayer = this.players.get(this.currentPlayer.getId().getValue() - 1);
    }
  }

  /**
   * Handles second starting settlement of player.
   *
   * @param currentPlayer current game player.
   */
  private void handleStartBuild2(Player currentPlayer) {
    ActionExecutor.SettlementGameStatePair sgsp = 
        this.actionExecutor.secondSettlement(currentPlayer, currentGameState);
    this.previousSettlement = sgsp.getSettlement();
    this.currentGameState = sgsp.getGameState();
    this.gameStates.add(currentGameState);
    this.currentState = State.START2_ROAD;
  }

  /**
   * Handles starting road of current player.
   *
   * @param currentPlayer current game player.
   */
  private void handleStartRoad(Player currentPlayer) {
    this.currentGameState = this.actionExecutor.startingSettlement(currentPlayer, currentGameState);
    this.gameStates.add(currentGameState);
    if (this.gameStates.size() >= 8) {
      this.currentState = State.START2_BUILD;
    } else {
      this.currentState = State.START_BUILD;
      this.currentPlayer = this.players.get(this.currentPlayer.getId().getValue() - 1);
    }
  }

  /**
   * Handles starting settlement of player.
   *
   * @param currentPlayer current game player.
   */
  private void handleStartBuild(Player currentPlayer) {
    this.currentGameState = this.actionExecutor.startingSettlement(currentPlayer, currentGameState);
    this.gameStates.add(currentGameState);
    this.currentState = State.START_ROAD;
  }

  /**
   * Represents the next turn of the Catan game.
   */
  public void nextTurn() {
    Player curPlayer = this.currentPlayer;
    this.makeMove(curPlayer);
  }

  /**
   * Produce resources based on dice roll.
   * If there are not enough resource cards to distribute among multiple
   * players, no players get any resource cards.
   * If there are not enough resource cards to distribute to a single player,
   * that player only recieves the rest of the resource cards for that resource.
   *
   * @param diceRoll the number of the dice roll
   */
  private void produce(int diceRoll) {
    HashMap<Player, HashMap<Terrain, Integer>> resources = this.getResourcesToProduce(diceRoll);

    HashMap<Terrain, Integer> amountToProduce = this.getAmountOfResourcesToProduce(resources);

    Set<Terrain> doNotProduce = this.getResourcesUnableToProduce(resources, amountToProduce);

    // Give all players resources for the roll
    // if resource is able to be produced.
    resources.forEach((player, terrainMap) -> {
      terrainMap.forEach((terrain, amount) -> {
        if (doNotProduce.contains(terrain)) {
          return;
        }
        for (int i = 0; i < amount && dealer.canDrawResource(terrain, 1); i++) {
          player.addToKnownCards(dealer.drawResource(terrain));
        }
      });
    });
  }

  /**
   * Get all the resources that the players would get for the given dice roll,
   * if each bank had enough resources for everyone.
   *
   * @param diceRoll the roll which will produce for the players
   * @return a map containing each player, and each terrain they are
   *         expecting production from
   */
  private HashMap<Player, HashMap<Terrain, Integer>> getResourcesToProduce(
      int diceRoll) {
    // Calculate the amount of resources which should be given to each player.
    HashMap<Player, HashMap<Terrain, Integer>> resources = new HashMap<>();
    for (Tile t : this.board.getTiles()) {
      if (t.getTerrainChit().getValue() == diceRoll) {
        for (Node n : t.getNodes()) {
          if (n.hasBuilding()) {
            int resourceAmt;
            if (n.getBuilding() instanceof Settlement) {
              resourceAmt = 1;
            } else {
              resourceAmt = 2;
            }

            Player p = this.players.get(n.getBuilding().getPlayerId().getValue());
            resources.putIfAbsent(p, new HashMap<Terrain, Integer>());
            resources.get(p).merge(
                t.getTerrain(),
                resourceAmt,
                (oldValue, newValue) -> {
                  return oldValue + newValue;
                });
          }
        }
      }
    }
    return resources;
  }

  /**
   * Calculate the amount of each resource which needs to be produced based on
   * a map of each player's expected resource income.
   *
   * @param resources map of each player's expected resource income
   * @return amount of resources needed to give all player's their expected
   *         resource income
   */
  private HashMap<Terrain, Integer> getAmountOfResourcesToProduce(
      HashMap<Player, HashMap<Terrain, Integer>> resources) {
    // For each resource, determine the amount which the bank has to produce
    // to give to all players.
    HashMap<Terrain, Integer> amountToProduce = new HashMap<Terrain, Integer>();
    resources.forEach((player, terrainMap) -> terrainMap
        .forEach((terrain, amount) -> amountToProduce.merge(terrain, amount, (oldAmt, newAmt) -> {
          return oldAmt + newAmt;
        })));
    return amountToProduce;
  }

  /**
   * Get the resources which cannot be produced based on the amount of
   * resources in the bank vs. the amount of resources expected by the players
   * given the roll of the dice. Resources can be given if the bank would be
   * depleated but all resources of that type would be given to one player,
   * otherwise the bank cannot allocate any resources to any players.
   *
   * @param resources       map of each player's expected resource income
   * @param amountToProduce amount of resources needed to give all player's their
   *                        expected resource income
   * @return the set of terrains which cannot be produced
   */
  private Set<Terrain> getResourcesUnableToProduce(
      HashMap<Player, HashMap<Terrain, Integer>> resources,
      HashMap<Terrain, Integer> amountToProduce) {
    // Construct a set of resources which cannot be produced due to the
    // size of the resource bank being to small.
    Set<Terrain> doNotProduce = new HashSet<Terrain>();
    amountToProduce.forEach((terrain, amount) -> {
      if (dealer.canDrawResource(terrain, amount)) {
        return;
      }
      resources.forEach((player, terrainMap) -> {
        if (terrainMap.get(terrain) == amount) {
          return;
        }
      });
      doNotProduce.add(terrain);
    });
    return doNotProduce;
  }

  /**
   * Simulates rolling 2 dice and returns the output.
   *
   * @return a value between 2-12 representing two d6 rolls.
   */
  private int rollDice() {
    Random rand = new Random();
    int low = 1;
    int high = 6;
    int dice1Value = rand.nextInt(high - low) + low;
    int dice2Value = rand.nextInt(high - low) + low;
    this.lastDiceRollValue = dice1Value + dice2Value;
    this.updateGamestate();
    return dice1Value + dice2Value;
  }

  //****************************************************************************
  //************************* RLEnv Interface Methods **************************
  //****************************************************************************
  @Override
  public void reset() {
    this.resetHelper();
  }

  @Override
  public NDList getObservation() {
    return this.currentGameState.asNDList();
  }

  @Override
  public ActionSpace getActionSpace() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getActionSpace'");
  }

  @Override
  public Step step(NDList action, boolean training) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'step'");
  }

  @Override
  public Step[] getBatch() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getBatch'");
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'close'");
  }

  public float runEnvironment(ArrayList<RlAgent> agents, boolean training) {
    throw new NotImplementedException("runEnvironment not implement yet.");
  }

  //****************************************************************************
  //*************************** Getters and Setters ****************************
  //****************************************************************************

  public List<Player> getPlayers() {
    return this.players;
  }

  public Player getCurrentPlayer() {
    return this.currentPlayer;
  }

  public Board getBoard() {
    return this.board;
  }

  public Dealer getDealer() {
    return this.dealer;
  }

  public GameState getCurrentGameState() {
    return this.currentGameState;
  }

  public List<GameState> getGameStates() {
    return this.gameStates;
  }

  public boolean hasEnded() {
    return this.ended;
  }

  public int getLastDiceRollValue() {
    return this.lastDiceRollValue;
  }

  /**
   * Whether or not the current game is in it's starting turns or not.
   *
   * @return whether the game is in its starting turns or not.
   */
  public boolean startingTurnSettlement() {
    return this.currentState == State.START_BUILD 
        || this.currentState == State.START2_BUILD;
  }

  public boolean startingTurnRoad() {
    return this.currentState == State.START_ROAD 
        || this.currentState == State.START2_ROAD;
  }
}
