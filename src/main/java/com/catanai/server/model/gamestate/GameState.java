package com.catanai.server.model.gamestate;

import com.catanai.server.model.Game;
import com.catanai.server.model.bank.ResourceBank;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.building.Building;
import com.catanai.server.model.board.building.Settlement;
import com.catanai.server.model.board.graph.Edge;
import com.catanai.server.model.board.graph.Node;
import com.catanai.server.model.board.tile.Port;
import com.catanai.server.model.board.tile.Tile;
import com.catanai.server.model.player.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Respresents all Catan game data as arrays of integers.
* The GameState can be used as a sort of "minified" game, where a
* a game at a given turn could be reconstructed with a single gamestate 
* from a certain player's perspective.
*/
// public final class GameState implements RlEnv.Step {
public final class GameState {
  /** The game which this gamestate refers to, i.e. current board state. */
  private Game game;
  
  /** Contains information about tile chit and terrain values. */
  private int[][] tiles;
  
  /** Contains information about all the current bank sizes. */
  private int[] banks;
  
  /** 
  * Contains a list of all players currently known resource cards.
  * If the player is the current player, has all of their resource cards.
  * To note: cards of a player which were possibly stolen via knight from this
  * player's perspective are not included in the other player's cards.
  */
  private int[][] playerPerspectiveResourceCards;
  
  /**
  * Contains a list of all players resource cards in hand.
  */
  private int[][] playerFullResourceCards;
  
  /** Contains information about road placement on each edge. */
  private int[] edges;
  
  /** Contains information about settlement, city placement on each node. */
  private int[][] nodes;
  
  /** Contains information about the ports on the board. */
  private int[] ports;
  
  /** Contains additional player information for each player, including:
  *   - Current number of victory points.
  *   - Whether the player has largest army.
  *   - Whether the player has longest road.
  *   - The amount of settlements, cities and roads the player has left.
  *   - The number of knights played.
  *   - The amount of development cards in hand.
  */
  private int[][] playerMetadata;
  
  /** Contains the last dice roll value. */
  private int lastDiceRollValue;

  /** Contains information on the last action played by the player. */
  private int lastAction;
  
  /** Order of all the cards. This will stay consistent whenever reffering to cards. */
  private final ResourceCard[] orderOfCards = {
    ResourceCard.WOOL,
    ResourceCard.GRAIN,
    ResourceCard.LUMBER,
    ResourceCard.ORE,
    ResourceCard.BRICK,
  };
  
  /**
   * Creates a new gamestate according to @param game.
   *
   * @param game game of catan at a certain point in time.
   */
  public GameState(Game game) {
    this.game = game;
    this.tiles = new int[19][2];
    this.banks = new int[6];
    this.playerPerspectiveResourceCards = new int[4][5];
    this.playerFullResourceCards = new int[4][5];
    this.edges = new int[72];
    this.nodes = new int[54][2];
    this.ports = new int[9];
    this.playerMetadata = new int[4][8];
    this.lastDiceRollValue = game.getLastDiceRollValue();
    this.lastAction = game.getLastAction();
    
    // Populate all class variables.
    this.populateTiles();
    this.populateBanks();
    this.populatePlayerPerspectiveResourceCards();
    this.populatePlayerFullResourceCards();
    this.populateEdges();
    this.populateNodes();
    this.populatePorts();
    this.populatePlayerMetadata();
  }
  
  /**
  * Populates the tiles variable with the current gamestate's tiles.
  * Example:
  * [ [0, 4], [2, 5] ]
  *   -> Tile 0 has terrain 0 and terrain chit 4
  *   -> Tile 1 has terrain 2 and terrain chit 5
  */
  private void populateTiles() {
    ArrayList<Tile> tiles = game.getBoard().getTiles();
    for (int i = 0; i < tiles.size(); i++) {
      Tile curTile = tiles.get(i);
      this.tiles[i] = new int[] {
        curTile.getTerrain().getValue(),
        curTile.getTerrainChit().getValue()
      };
    }
  }
  
  /**
  * Populate the banks with the current game's bank values.
  * Example:
  * [ 12, 14, 6, 8, 1, 7 ] represents:
  *   -> the wool bank has a size of 12
  *   -> the grain bank has a size of 14
  *   -> the lumber bank has a size of 6
  *   -> the ore bank has a size of 8
  *   -> the brick bank has a size of 1
  *   -> the development bank has a size of 7
  */
  private void populateBanks() {
    HashMap<ResourceCard, ResourceBank> banks = this.game.getDealer().getResourceBanks();
    ResourceCard[] orderOfBanks = {
      ResourceCard.WOOL,
      ResourceCard.GRAIN,
      ResourceCard.LUMBER,
      ResourceCard.ORE,
      ResourceCard.BRICK,
    };
    // Add resource bank sizes.
    for (int i = 0; i < orderOfBanks.length; i++) {
      ResourceCard curCard = orderOfBanks[i];
      ResourceBank curBank = banks.get(curCard);
      this.banks[i] = curBank.getCurrentBankSize();
    }
    // Add development bank size.
    int devBankSize = this.game.getDealer().getDevelopmentBank().getCurrentBankSize();
    this.banks[5] = devBankSize;
  }
  
  /**
  * Populate player perspective cards with all the other player's cards.
  * i.e. according to the current player's perspective, what cards do they know other players have
  * Example:
  * [ [3, 1, 0, 2, 4], ... ]
  *   -> The first player has:
  *     -> 3 wool
  *     -> 1 grain
  *     -> 0 lumber
  *     -> 2 ore
  *     -> 4 brick
  * etc... until all players cards are populated.
  */
  private void populatePlayerPerspectiveResourceCards() {
    for (int i = 0; i < this.game.getPlayers().size(); i++) {
      Player curPlayer = this.game.getPlayers().get(i);
      List<ResourceCard> curCards = 
          curPlayer.equals(this.game.getCurrentPlayer()) 
          ? curPlayer.getResourceCards()
          : curPlayer.getKnownCards();
      // Initialize HashMap.
      HashMap<ResourceCard, Integer> numCards = new HashMap<>();
      for (ResourceCard card : orderOfCards) {
        numCards.put(card, 0);
      }
      // Add to hashmap the number of cards of type found.
      for (ResourceCard card : curCards) {
        numCards.merge(card, 1, (a, b) -> a + b);
      }
      // Add cards to append to playerResourceCards.
      ArrayList<Integer> toAddToPlayerResourceCards = new ArrayList<Integer>();
      for (ResourceCard card : orderOfCards) {
        toAddToPlayerResourceCards.add(numCards.get(card));
      }
      playerPerspectiveResourceCards[i] = new int[5];
      for (int j = 0; j < playerPerspectiveResourceCards[i].length; j++) {
        playerPerspectiveResourceCards[i][j] = toAddToPlayerResourceCards.get(j);
      }
    }
  }
  
  /**
  * Populate player full cards with all the other player's cards.
  * Example:
  * [ [3, 1, 0, 2, 4], ... ]
  *   -> The first player has:
  *     -> 3 wool
  *     -> 1 grain
  *     -> 0 lumber
  *     -> 2 ore
  *     -> 4 brick
  * etc... until all players cards are populated.
  */
  private void populatePlayerFullResourceCards() {
    for (int i = 0; i < this.game.getPlayers().size(); i++) {
      Player curPlayer = this.game.getPlayers().get(i);
      List<ResourceCard> curCards = curPlayer.getResourceCards();
      // Initialize HashMap.
      HashMap<ResourceCard, Integer> numCards = new HashMap<>();
      for (ResourceCard card : orderOfCards) {
        numCards.put(card, 0);
      }
      // Add to hashmap the number of cards of type found.
      for (ResourceCard card : curCards) {
        numCards.merge(card, 1, (a, b) -> a + b);
      }
      // Add cards to append to playerResourceCards.
      ArrayList<Integer> toAddToPlayerResourceCards = new ArrayList<Integer>();
      for (ResourceCard card : orderOfCards) {
        toAddToPlayerResourceCards.add(numCards.get(card));
      }
      playerFullResourceCards[i] = new int[5];
      for (int j = 0; j < playerFullResourceCards[i].length; j++) {
        playerFullResourceCards[i][j] = toAddToPlayerResourceCards.get(j);
      }
    }
  }
  
  /**
  * Populate edges with all the edges on the board.
  * Example:
  * [ 3, 4, 0, 1, 0, 2, ... ]
  *   -> The first edge has a road from player 3.
  *   -> The second edge has a road from player 4.
  *   -> The third edge has no road.
  *   -> The fourth edge has a road from player 1.
  *   -> The fifth edge has no road.
  *   -> The sixth edge has a road from player 2.
  * etc... for all 72 possible edges on the board.
  */
  private void populateEdges() {
    ArrayList<Edge> gameEdges = this.game.getBoard().getEdges();
    for (int i = 0; i < this.edges.length; i++) {
      if (!gameEdges.get(i).hasRoad()) {
        this.edges[i] = 0;
      } else {
        this.edges[i] = gameEdges.get(i).getRoad().getPlayerId().getValue();
      }
    }
  }
  
  /**
  * Populate nodes with all the nodes on the board.
  * Example:
  * [ [0, 0], [1, 1], [0, 0], [2, 2], ... ]
  *   -> No player has a building on the first node.
  *   -> Player 1 has a settlement on the second node.
  *   -> No player has a building on the third node.
  *   -> Player 2 has a settlement on the fourth node.
  * etc... for all 54 possible nodes on the board.
  */
  private void populateNodes() {
    ArrayList<Node> boardNodes = this.game.getBoard().getNodes();
    for (int i = 0; i < this.nodes.length; i++) {
      if (!boardNodes.get(i).hasBuilding()) {
        this.nodes[i] = new int[] { 0, 0 };
      } else {
        Building buildingAtCurNode = boardNodes.get(i).getBuilding();
        int buildingCode = buildingAtCurNode instanceof Settlement ? 1 : 2;
        this.nodes[i] = new int[] {
          buildingAtCurNode.getPlayerId().getValue(),
          buildingCode
        };
      }
    }
  }
  
  /**
  * Populate ports with all the ports on the board.
  * Example:
  * [ 1, 2, ... ]
  *   -> Port one (connected to nodes 0, 3) is a three-to-one port.
  *   -> Port two (connected to nodes 1, 5) is a grain two-to-one port.
  * etc... for all 9 ports on the board.
  */
  private void populatePorts() {
    ArrayList<Node> nodes = this.game.getBoard().getNodes();
    int[] nodeWithPortIndexes = {
      0, 1, 10, 11, 26, 33, 42, 47, 49
    };
    
    for (int i = 0; i < nodeWithPortIndexes.length; i++) {
      Port p = nodes.get(nodeWithPortIndexes[i]).getPort();
      this.ports[i] = p.getValue();
    }
  }
  
  /**
  * Populate all player's metadata.
  * Example:
  * [ [2, 0, 0, 3, 4, 12, 0, 0], [...], ... ]
  *   -> Player one has:
  *     -> 2 victory points
  *     -> Does not have largest army
  *     -> Does not have longest road
  *     -> Has 3 settlements remaining
  *     -> Has 4 cities remaining
  *     -> Has 12 roads remaining
  *     -> Has played 0 knights
  *     -> Has 0 development cards in hand
  * etc... for all 4 players of the game.
  */
  private void populatePlayerMetadata() {
    List<? extends Player> players = this.game.getPlayers();
    for (int i = 0; i < players.size(); i++) {
      Player curPlayer = players.get(i);
      int[] curPlayerMetadata = new int[8];
      curPlayerMetadata[0] = curPlayer.getVictoryPoints();
      curPlayerMetadata[1] = curPlayer.hasLargestArmy() ? 1 : 0;
      curPlayerMetadata[2] = curPlayer.hasLongestRoad() ? 1 : 0;
      curPlayerMetadata[3] = curPlayer.getRemainingSettlements();
      curPlayerMetadata[4] = curPlayer.getRemainingCities();
      curPlayerMetadata[5] = curPlayer.getRemainingRoads();
      curPlayerMetadata[6] = curPlayer.getNumKnightsPlayed();
      curPlayerMetadata[7] = curPlayer.getDevelopmentCards().size();
      this.playerMetadata[i] = curPlayerMetadata;
    }
  }
  
  //****************************************************************************
  //********************************** Getters *********************************
  //****************************************************************************
  
  public int[][] getTiles() {
    return this.tiles;
  }
  
  public int[] getBanks() {
    return this.banks;
  }
  
  public int[][] getPlayerPerspectiveResourceCards() {
    return this.playerPerspectiveResourceCards;
  }
  
  public int[][] getPlayerFullResourceCards() {
    return this.playerFullResourceCards;
  }
  
  public int[] getEdges() {
    return this.edges;
  }
  
  public int[][] getNodes() {
    return this.nodes;
  }
  
  public int[] getPorts() {
    return this.ports;
  }
  
  public int[][] getPlayerMetadata() {
    return this.playerMetadata;
  }
  
  public int getLastDiceRollValue() {
    return this.lastDiceRollValue;
  }
  
  public ResourceCard[] getOrderOfCards() {
    return this.orderOfCards;
  }

  public int[][][] toArray() {
    return new int[][][] {
      tiles,
      new int[][] {banks},
      playerPerspectiveResourceCards,
      playerFullResourceCards,
      new int[][] {edges},
      nodes,
      new int[][] {ports},
      playerMetadata
    };
  }

  public Map<String, int[][]> toMap() {
    Map<String, int[][]> map = new HashMap<>();
    map.put("tiles", this.tiles);
    map.put("banks", new int[][] {banks});
    map.put("playerPerspectiveResourceCards", this.playerPerspectiveResourceCards);
    map.put("playerFullResourceCards", this.playerFullResourceCards);
    map.put("edges", new int[][] {this.edges});
    map.put("nodes", this.nodes);
    map.put("ports", new int[][] {this.ports});
    map.put("playerMetadata", this.playerMetadata);
    map.put("lastRoll", new int[][] {{this.lastDiceRollValue}});
    map.put("currentPlayer", new int[][] {{this.game.getCurrentPlayer().getId().getValue()}});
    int[][] orderOfCards = new int[][] {{
        this.orderOfCards[0].getValue(),
        this.orderOfCards[1].getValue(),
        this.orderOfCards[2].getValue(),
        this.orderOfCards[3].getValue(),
        this.orderOfCards[4].getValue(),
      }
    };
    map.put("orderOfCards", orderOfCards);
    map.put("actionID", new int[][] {{this.lastAction}});
    map.put("finished", new int[][] {{this.game.hasEnded() ? 1 : 0}});

    return map;
  }
}
