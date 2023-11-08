package com.catanai.server.model.action;

import com.catanai.server.model.Game;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.PlayerID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTradeActionState {
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

  /**
   * Tests the trading action state.
   */
  @Test
  public void testTradeActionState() {
    // Get through starting turns.
    for (int i = 0; i < 8; i++) {
      Assert.assertTrue(this.game.nextMove() && this.game.nextMove()); // Settlement and road.
    }
    DeterministicPlayer p = this.players.get(0);
    p.addNextMove(new int[] { 15, 0 }); // Roll dice.
    game.nextMove();

    HashMap<Player, ResourceCard> trade = new HashMap<Player, ResourceCard>();

    while (trade.keySet().size() < 2) {
      for (Player player : game.getPlayers()) {
        if (player.getAmountOfResourceCardsInHand() > 0) {
          player.getResourceCards().forEach((k, v) -> {
            if (v > 0) {
              trade.put(player, k);
            }
          });
        } else {
          p.addNextMove(new int[] {});
        }
      }
    }

  }
}
