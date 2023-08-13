package com.catanai.server.model;

import com.catanai.server.model.board.Board;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.PlayerId;
/** Utils. */
import java.util.ArrayList;
import java.util.List;
/** Test. */
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests Game functionality; player's turns, etc.
 */
public class TestGame {
  private Game game;
  private List<Player> players;
  
  @Before
  public void setUp() {
    List<Player> players = new ArrayList<>();
    players.add(new DeterministicPlayer(PlayerId.ONE));
    players.add(new DeterministicPlayer(PlayerId.TWO));
    players.add(new DeterministicPlayer(PlayerId.THREE));
    players.add(new DeterministicPlayer(PlayerId.FOUR));
    game = new Game(players);
  }

  @Test
  public void testGameInitialization() {
    Assert.assertTrue(game.getBoard().getClass().equals(Board.class));
  }
}