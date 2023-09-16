package com.catanai.server.model;

import com.catanai.server.model.board.Board;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.PlayerID;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests Game functionality; player's turns, etc.
 */
public class TestGame {
  private Game game;
  
  /**
   * Setup game for future test cases.
   */
  @Before
  public void setUp() {
    List<Player> players = new ArrayList<>();
    players.add(new DeterministicPlayer(PlayerID.ONE));
    players.add(new DeterministicPlayer(PlayerID.TWO));
    players.add(new DeterministicPlayer(PlayerID.THREE));
    players.add(new DeterministicPlayer(PlayerID.FOUR));
    game = new Game(players);
  }

  @Test
  public void testGameInitialization() {
    Assert.assertTrue(game.getBoard().getClass().equals(Board.class));
  }
}