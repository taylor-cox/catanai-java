package com.catanai.server.model.action;

import com.catanai.server.model.Game;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.PlayerID;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestActionStateBusinessAsUsual {
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
        { { 2, 0 }, { 1, 0 }, { 2, 16 }, { 1, 23 } }, // Player 1 Moves
        { { 2, 1 }, { 1, 2 }, { 2, 10 }, { 1, 16 } }, // Player 2 Moves
        { { 2, 2 }, { 1, 4 }, { 2, 9 }, { 1, 14 } }, // Player 3 Moves
        { { 2, 7 }, { 1, 10 }, { 2, 8 }, { 1, 12 } }, // Player 4 Moves
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
        this.game.getActionExecutor().getActionStateMachine().getCurrentActionState());
  }
}
