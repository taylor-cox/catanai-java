package com.catanai.server.model.action;

import com.catanai.server.model.Game;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.PlayerID;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestActionStateFinished {
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
  public void testFinishedActionState() {

  }
}
