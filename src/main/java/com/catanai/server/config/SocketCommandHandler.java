package com.catanai.server.config;

import com.catanai.server.model.Game;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.PlayerId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.NotImplementedException;

/**
 * Socket command handler.
 */
public class SocketCommandHandler {
  String command;
  Game game;

  /**
   * Handles command from websocket.
   *
   * @param command command to handle from websocket.
   * @return string of command output.
   */
  public String handleCommand(String command) {
    switch (command) {
      case "newGame":
        List<DeterministicPlayer> players = new ArrayList<DeterministicPlayer>();
        players.add(new DeterministicPlayer(PlayerId.ONE));
        players.add(new DeterministicPlayer(PlayerId.TWO));
        players.add(new DeterministicPlayer(PlayerId.THREE));
        players.add(new DeterministicPlayer(PlayerId.FOUR));
        game = new Game(players);
        return Arrays.deepToString(game.getCurrentGameState().toArray());
      case "getCurrentGamestate":
        return this.game.getCurrentGameState().toArray().toString();
      case "makeMove":
        throw new NotImplementedException("Not implemented");
        // break;
      default:
        return "";
    }
  }
}
