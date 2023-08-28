package com.catanai.server.config;

import com.catanai.server.model.Game;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.PlayerId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.NotImplementedException;
import org.json.JSONArray;

/**
 * Socket command handler.
 */
public class SocketCommandHandler {
  String command;
  Game game;
  List<DeterministicPlayer> players;

  /**
   * Handles command from websocket.
   *
   * @param command command to handle from websocket.
   * @return string of command output.
   */
  public String handleCommand(String command, String action) {
    switch (command) {
      case "newGame":
        this.players = new ArrayList<DeterministicPlayer>();
        this.players.add(new DeterministicPlayer(PlayerId.ONE));
        this.players.add(new DeterministicPlayer(PlayerId.TWO));
        this.players.add(new DeterministicPlayer(PlayerId.THREE));
        this.players.add(new DeterministicPlayer(PlayerId.FOUR));
        this.game = new Game(players);
        return this.currentGameStateAsJSON();
      case "getCurrentGamestate":
        return this.currentGameStateAsJSON();
      case "makeMove":
        throw new NotImplementedException("Not implemented");
        DeterministicPlayer currentPlayer = (DeterministicPlayer) this.game.getCurrentPlayer();
        JSONArray jsonArray = new JSONArray(action);
        currentPlayer.setNextMoveMetadata();
        // this.game.
        // break;
      default:
        return "";
    }
  }

  private String currentGameStateAsJSON() {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(this.game.getCurrentGameState().toMap());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "ERROR";
    }
  }
}
