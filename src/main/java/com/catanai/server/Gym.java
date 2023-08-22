package com.catanai.server;

import com.catanai.server.model.Game;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.PlayerId;
import com.catanai.server.model.player.ReinforcementLearningPlayer;

import java.util.ArrayList;

/**
 * <i> *que rocky music* </i>.
 * Training for the Reinforcement learning player.
 */
public final class Gym {
  final long inputSize = 286;
  final long outputSize = 11;

  public static void main(String[] args) {
    ArrayList<Player> players = new ArrayList<Player>();
    initializePlayers(players);
    Game game = new Game(players);
    game.runEnvironment(null, false);
  }

  private static void initializePlayers(ArrayList<Player> players) {
    players.add(new ReinforcementLearningPlayer(PlayerId.ONE));
    players.add(new ReinforcementLearningPlayer(PlayerId.TWO));
    players.add(new ReinforcementLearningPlayer(PlayerId.THREE));
    players.add(new ReinforcementLearningPlayer(PlayerId.FOUR));
  }
}
