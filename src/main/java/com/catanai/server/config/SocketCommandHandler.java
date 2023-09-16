package com.catanai.server.config;

import com.catanai.server.model.Game;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.PlayerID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;

/**
 * Socket command handler.
 */
public class SocketCommandHandler {
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
        return this.currentGameStateAsJSON(0);
      case "makeMove":
        return this.handleMakeMove();
      case "addPlayerMove":
        return this.handleAddPlayerMove(action, playerID);
      default:
        return "ERROR: command not handled.";
    }
  }

  private String handleAddPlayerMove(String action, String playerID) {
    Optional<DeterministicPlayer> playerToAddActions = this.players
        .stream()
        .filter(player -> player.getID().toString().equals(playerID))
        .findFirst();

    if (playerToAddActions.isEmpty()) {
      throw new RuntimeException("Player of ID " + playerID + " to add actions to is not available in game.");
    }

    JSONArray jsonActionArr = new JSONArray(action);
    int[] intActionArr = new int[jsonActionArr.length()];
    for (int i = 0; i < jsonActionArr.length(); ++i) {
      intActionArr[i] = jsonActionArr.optInt(i);
    }

    playerToAddActions.get().addNextMove(intActionArr);
    return "{\"success\": true}";
  }

  private String handleMakeMove() {
    int reward = -1;
    boolean successful = this.game.nextMove();
    if (successful) {
      reward = 1;
    }
    return this.currentGameStateAsJSON(reward);
  }

  private String handleNewGame() {
    this.players = new ArrayList<DeterministicPlayer>();
    this.players.add(new DeterministicPlayer(PlayerID.ONE));
    this.players.add(new DeterministicPlayer(PlayerID.TWO));
    this.players.add(new DeterministicPlayer(PlayerID.THREE));
    this.players.add(new DeterministicPlayer(PlayerID.FOUR));
    this.game = new Game(players);
    return this.currentGameStateAsJSON(0);
  }

  private String currentGameStateAsJSON(Integer reward) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      Map<String, int[][]> gameStateMap = this.game.getCurrentGameState().toMap();
      gameStateMap.put("reward", new int[][]{{reward}});
      return objectMapper.writeValueAsString(gameStateMap);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "ERROR";
    }
  }
}
