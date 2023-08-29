package com.catanai.server.config;

import com.catanai.server.model.Game;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.PlayerId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
  public String handleCommand(String command, String action, String playerID) {
    switch (command) {
      case "newGame":
        return this.handleNewGame();
      case "getCurrentGamestate":
        return this.currentGameStateAsJSON();
      case "makeMove":
        return this.handleMakeMove();
      case "addPlayerMove":
        return this.handleAddPlayerMove(action, playerID);
      default:
        return "ERROR: command not handled.";
    }
  }

  private String handleAddPlayerMove(String action, String playerID) {
    DeterministicPlayer playerToAddActions = this.players
      .stream()
      .filter(player -> player.getId().toString().equals(playerID))
      .findFirst()
      .get();
    JSONArray jsonActionArr = new JSONArray(action);
    int[] intActionArr = new int[jsonActionArr.length()];
    for (int i = 0; i < jsonActionArr.length(); ++i) {
      intActionArr[i] = jsonActionArr.optInt(i);
    }

    playerToAddActions.addNextMove(intActionArr);
    return "{\"success\": true}";
  }

  private String handleMakeMove() {
    this.game.nextMove();
    return this.currentGameStateAsJSON();
  }

  private String handleNewGame() {
    this.players = new ArrayList<DeterministicPlayer>();
    this.players.add(new DeterministicPlayer(PlayerId.ONE));
    this.players.add(new DeterministicPlayer(PlayerId.TWO));
    this.players.add(new DeterministicPlayer(PlayerId.THREE));
    this.players.add(new DeterministicPlayer(PlayerId.FOUR));
    this.game = new Game(players);
    return this.currentGameStateAsJSON();
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
