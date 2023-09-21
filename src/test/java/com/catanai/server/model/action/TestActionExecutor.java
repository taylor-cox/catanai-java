package com.catanai.server.model.action;

import com.catanai.server.model.Game;
import com.catanai.server.model.bank.Dealer;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.building.Road;
import com.catanai.server.model.board.graph.Edge;
import com.catanai.server.model.board.graph.Node;
import com.catanai.server.model.board.tile.Terrain;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.PlayerID;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests the action executor.
 */
public class TestActionExecutor {
  private Game game;
  private List<DeterministicPlayer> players;
  private int[][][] startingMoves;

  /**
   * Setup game for future test cases.
   */
  @Before
  public void setUp() {
    this.players = new ArrayList<DeterministicPlayer>();
    players.add(new DeterministicPlayer(PlayerID.ONE));
    players.add(new DeterministicPlayer(PlayerID.TWO));
    players.add(new DeterministicPlayer(PlayerID.THREE));
    players.add(new DeterministicPlayer(PlayerID.FOUR));
    this.game = new Game(players);
    
    this.startingMoves = new int[][][] {
      {{ 2, 0 }, { 1, 0  }, { 2, 16 }, { 1, 23 }}, // Player 1 Moves
      {{ 2, 1 }, { 1, 2  }, { 2, 10 }, { 1, 16 }}, // Player 2 Moves
      {{ 2, 2 }, { 1, 4  }, { 2, 9  }, { 1, 14 }}, // Player 3 Moves
      {{ 2, 7 }, { 1, 10 }, { 2, 8  }, { 1, 12 }}, // Player 4 Moves
    };

    for (DeterministicPlayer p : this.players) {
      p.addAllMoves(this.startingMoves[p.getID().getValue() - 1]);
    }
  }

  /**
   * Teardown class variables after tests.
   */
  @After
  public void tearDown() {
    this.game = null;
    this.players = null;
    this.startingMoves = null;
  }

  @Test
  public void testFirstStartingTurns() {
    // First starting moves.
    for (int i = 0; i < 4; i++) {
      DeterministicPlayer p = this.players.get(i);
      Assert.assertTrue(this.game.getCurrentPlayer().equals(p));

      //------------------------------Settlement Testing------------------------
      int[] settlementMove = this.startingMoves[p.getID().getValue() - 1][0];
      Assert.assertTrue(this.game.nextMove());

      Node playerFirstSettleNode = this.game.getBoard().getNodes().get(settlementMove[1]);

      // Check player's amount of remaining settlements.
      Assert.assertEquals(4, p.getRemainingSettlements());

      // Check player's victory points.
      Assert.assertEquals(1, p.getVictoryPoints());

      // Check player's first starting settlement is correct.
      Assert.assertNotNull(p.getFirstStartingSettlement());
      Assert.assertTrue(p.getFirstStartingSettlement().equals(playerFirstSettleNode.getBuilding()));

      // Check player's possible road edges.
      Assert.assertTrue(p.getPossibleRoadEdges().containsAll(playerFirstSettleNode.getConnectedEdges()));
      if (i != 3) {
        Assert.assertTrue(p.getPossibleRoadEdges().size() == 2);
      } else {
        Assert.assertTrue(p.getPossibleRoadEdges().size() == 3);
      }

      // Check player's possible city nodes.
      Assert.assertTrue(p.getPossibleCityNodes().contains(playerFirstSettleNode));

      // Check player's possible settlement nodes.
      Assert.assertTrue(p.getPossibleSettlementNodes().size() == 0);

      //---------------------------------Road Testing---------------------------
      int[] roadMove = this.startingMoves[p.getID().getValue() - 1][1];
      Assert.assertTrue(this.game.nextMove());

      // Test edge where road was placed has the road of the player.
      Edge playerRoadEdge = this.game.getBoard().getEdges().get(roadMove[1]);
      Assert.assertTrue(playerRoadEdge.getRoad().getPlayerId() == p.getID());

      List<Edge> possibleRoadEdges = this.game.getBoard().getEdges().stream()
          .map(e -> new Road(e.getIndex(), p.getID()))
          .filter(r -> this.game.getBoard().canPlaceRoad(r))
          .map(r -> this.game.getBoard().getEdges().get(r.getPlacement()))
          .collect(Collectors.toList());

      // Check player's amount of remaining roads.
      Assert.assertEquals(14, p.getRemainingRoads());

      // Check player's possible road edges.
      Assert.assertTrue(p.getPossibleRoadEdges().containsAll(possibleRoadEdges));
    }
  }

  @Test
  public void testSecondStartingTurns() {
    // First Starting Moves
    for (int i = 0; i < 4; i++) {
      DeterministicPlayer p = this.players.get(i);
      Assert.assertEquals(p, this.game.getCurrentPlayer());
      // First Settlement placement
      Assert.assertTrue(this.game.nextMove());
      // First Road placement
      Assert.assertTrue(this.game.nextMove());
    }

    // Second Starting Moves
    for (int i = 3; i >= 0; i--) {
      DeterministicPlayer p = this.players.get(i);
      Assert.assertEquals(p.getID(), this.game.getCurrentPlayer().getID());

      //------------------------------Settlement Testing------------------------
      int[] settlementMove = this.startingMoves[p.getID().getValue() - 1][2];
      // Test making the move on the board.
      Assert.assertTrue(this.game.nextMove());

      // Test the player received the resource cards associated with the node.
      Node settlementNode = this.game.getBoard().getNodes().get(settlementMove[1]);
      List<Terrain> settlementProduces = settlementNode.getProduces();
      // Assert.assertTrue(settlementProduces.size() == 3);
      Dealer dealer = game.getDealer();
      for (Terrain t : settlementProduces) {
        ResourceCard rc = dealer.getAssociatedResourceCard(t);
        if (rc == null) {
          continue;
        }
        Assert.assertTrue(p.getResourceCards().get(rc) > 0);
      }

      // Check the player's remaining settlements.
      Assert.assertEquals(3, p.getRemainingSettlements());

      // Check the player's victory points.
      Assert.assertEquals(2, p.getVictoryPoints());

      // Check the player's second starting settlement is correct.
      Assert.assertNotNull(p.getSecondStartingSettlement());

      // Check that the node has the player's second starting settlement.
      Assert.assertTrue(p.getSecondStartingSettlement().equals(settlementNode.getBuilding()));

      // Check the player's possible road edges.
      Assert.assertTrue(p.getPossibleRoadEdges().containsAll(settlementNode.getConnectedEdges()));

      // Check the player's possible city nodes.
      Assert.assertTrue(p.getPossibleCityNodes().contains(settlementNode));

      //---------------------------------Road Testing---------------------------
      Assert.assertTrue(this.game.nextMove());
      int[] roadMove = this.startingMoves[p.getID().getValue() - 1][3];

      // Test edge where road was placed has the road of the player.
      Edge playerRoadEdge = this.game.getBoard().getEdges().get(roadMove[1]);
      Assert.assertTrue(playerRoadEdge.getRoad() instanceof Road);

      // Check the player's remaining roads.
      Assert.assertEquals(13, p.getRemainingRoads());

      // Check the player's possible road edges.
      List<Edge> possibleRoadEdges = this.game.getBoard().getEdges().stream()
          .map(e -> new Road(e.getIndex(), p.getID()))
          .filter(r -> this.game.getBoard().canPlaceRoad(r))
          .map(r -> this.game.getBoard().getEdges().get(r.getPlacement()))
          .collect(Collectors.toList());
      Assert.assertTrue(p.getPossibleRoadEdges().containsAll(possibleRoadEdges));
    }
  }

  @Test
  public void testRollDiceActionState() {
    // Get through starting turns.
    for (int i = 0; i < 8; i++) {
      Assert.assertTrue(this.game.nextMove() && this.game.nextMove()); // Settlement and road.
    }
    DeterministicPlayer p = this.players.get(0);
    
    // Set player's next move to roll.
    p.addNextMove(new int[] { 15, 0 }); 
    
    // Check that the game's roll is 0 to start.
    Assert.assertEquals(0, this.game.getLastDiceRollValue());
    
    // Assert that the player rolls the dice.
    Assert.assertTrue(this.game.nextMove());

    // Check that the game's roll is sensible after rolling dice.
    Assert.assertTrue(this.game.getLastDiceRollValue() >= 2 && this.game.getLastDiceRollValue() <= 12);

    // If the roll causes a dicard, add move robber action to player.
    // We don't check if any player has to discard as no players have enough cards
    // after the starting turns to discard.
    if (this.game.getLastDiceRollValue() == 7) {
      int currentRobberIndex = this.game.getBoard().getTileIndexCurrentlyBlocked();
      int toBlockIndex = currentRobberIndex == 18 ? 17 : 18;
      
      p.addNextMove(new int[] { 12, toBlockIndex, 0 }); // Add move robber action, steal from no player.
      Assert.assertTrue(this.game.nextMove()); // Move robber.
    }
  }

  @Test
  public void testBusinessAsUsualActionState() {
    // Get through starting turns.
    for (int i = 0; i < 8; i++) {
      Assert.assertTrue(this.game.nextMove() && this.game.nextMove()); // Settlement and road.
    }
    DeterministicPlayer p = this.players.get(0);
    
    // Set player's next move to roll.
    p.addNextMove(new int[] { 15, 0 }); 
    
    // Check that the game's roll is 0 to start.
    Assert.assertEquals(0, this.game.getLastDiceRollValue());
    
    // Assert that the player rolls the dice.
    Assert.assertTrue(this.game.nextMove());

    // Check that the game's roll is sensible after rolling dice.
    Assert.assertTrue(this.game.getLastDiceRollValue() >= 2 && this.game.getLastDiceRollValue() <= 12);

    // If the roll causes a dicard, add move robber action to player.
    // We don't check if any player has to discard as no players have enough cards
    // after the starting turns to discard.
    if (this.game.getLastDiceRollValue() == 7) {
      int currentRobberIndex = this.game.getBoard().getTileIndexCurrentlyBlocked();
      int toBlockIndex = currentRobberIndex == 18 ? 17 : 18;
      
      p.addNextMove(new int[] { 12, toBlockIndex, 0 }); // Add move robber action, steal from no player.
      Assert.assertTrue(this.game.nextMove()); // Move robber.
    }

    // Check that the game is in business as usual state.
    Assert.assertEquals(
        ActionState.BUSINESS_AS_USUAL,
        this.game.getActionExecutor().getActionStateMachine().getCurrentActionState()
    );
  }

  @Test
  public void testDiscardActionState() {
    // Get through starting turns.
    for (int i = 0; i < 8; i++) {
      Assert.assertTrue(this.game.nextMove() && this.game.nextMove()); // Settlement and road.
    }
    DeterministicPlayer p = this.players.get(0);
    
    // Set player's next move to roll.
    p.addNextMove(new int[] { 15, 0 }); 
    
    // Check that the game's roll is 0 to start.
    Assert.assertEquals(0, this.game.getLastDiceRollValue());
    
    // Assert that the player rolls the dice.
    Assert.assertTrue(this.game.nextMove());

    // Check that the game's roll is sensible after rolling dice.
    Assert.assertTrue(this.game.getLastDiceRollValue() >= 2 && this.game.getLastDiceRollValue() <= 12);

    // If the roll causes a dicard, add move robber action to player.
    // We don't check if any player has to discard as no players have enough cards
    // after the starting turns to discard.
    if (this.game.getLastDiceRollValue() == 7) {
      int currentRobberIndex = this.game.getBoard().getTileIndexCurrentlyBlocked();
      int toBlockIndex = currentRobberIndex == 18 ? 17 : 18;
      
      p.addNextMove(new int[] { 12, toBlockIndex, 0 }); // Add move robber action, steal from no player.
      Assert.assertTrue(this.game.nextMove()); // Move robber.
    }

    // Check that the game is in business as usual state.
    Assert.assertEquals(
        ActionState.BUSINESS_AS_USUAL,
        this.game.getActionExecutor().getActionStateMachine().getCurrentActionState()
    );
  }

  // Action State to Test
  /* FIRST_SETTLEMENT(0),
   * FIRST_ROAD(1),
   * SECOND_SETTLEMENT(2),
   * SECOND_ROAD(3),
   * BUSINESS_AS_USUAL(4),
   * ROLL_DICE(5),
   * DISCARD(6),
   * MOVE_ROBBER(7),
   * TRADE(8),
   * FINISHED(9);
   */

  @Test
  public void testMoveRobberActionState() {
    
  }

  @Test
  public void testTradeActionState() {

  }

  @Test
  public void testFinishedActionState() {

  }
}
