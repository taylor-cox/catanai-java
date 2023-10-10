package com.catanai.server;

import com.catanai.server.dao.GameStatesDAO;
import com.catanai.server.model.Game;
import com.catanai.server.model.gamestate.GameState;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.PlayerID;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Main endpoint for Catan AI webviewer.
 */
@Controller
@RequestMapping("api/v1")
@CrossOrigin(origins = "*")
public class GameStateController {

  @Autowired
  private GameStatesDAO gameStatesDAO;

  /**
   * Returns random game with starting moves determined.
   * TODO: remove.
   *
   * @return gamestate with one player move
   */
  @GetMapping(path = "/randomGame", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public GameState randomGame() {
    List<Player> players = new ArrayList<Player>(4);
    players.add(new DeterministicPlayer(PlayerID.ONE));
    players.add(new DeterministicPlayer(PlayerID.TWO));
    players.add(new DeterministicPlayer(PlayerID.THREE));
    players.add(new DeterministicPlayer(PlayerID.FOUR));

    int[][][] moves = new int[][][] {
      { {2, 0}, {1, 0}, {2, 2}, {1, 4} },
      { {2, 7}, {1, 10}, {2, 10}, {1, 17} },
      { {2, 16}, {1, 23}, {2, 20}, {1, 32} },
      { {2, 33}, {1, 49}, {2, 37}, {1, 53} },
    };

    int counter = 0;

    for (Player p : players) {
      if (!(p instanceof DeterministicPlayer)) {
        continue;
      }
      int[][] setMoves = moves[counter];
      ((DeterministicPlayer) p).addAllMoves(setMoves);
      counter += 1;
    }

    Game game = new Game(players);

    for (int i = 0; i < 16; i++) {
      game.nextMove();
    }

    return game.getCurrentGameState();
  }

  /**
   * Returns node edge mappings.
   *
   * @return string of node-edge mappings.
   */
  @GetMapping(path = "/nodeTileMappings", produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public String nodeTileMappings() {
    try (Scanner s = new Scanner(new File("./node_tile_mapping.txt"))) {
      String returnString = "";
      while (s.hasNextLine()) {
        returnString += s.nextLine();
      }
      s.close();
      return returnString;
    } catch (FileNotFoundException e) {
      return "";
    }
  }

  /**
   * Returns node-edge mappings.
   *
   * @return node-edge mappings.
   */
  @GetMapping(path = "/nodeEdgeMappings", produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public String nodeEdgeMappings() {
    try (Scanner s = new Scanner(new File("./node_edge_mapping.txt"))) {
      String returnString = "";
      while (s.hasNextLine()) {
        returnString += s.nextLine();
      }
      s.close();
      return returnString;
    } catch (FileNotFoundException e) {
      return "";
    }
  }

  /**
   * Returns game with the given id.
   *
   * @param id id of the game to return.
   * @return game with the given id.
   */
  @GetMapping(path = "game", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<Map<String, Object>> game(@RequestParam("gameId") int id) {
    return gameStatesDAO.getGameStates(id);
  }

  /**
   * Returns the row ids representing the first turns in all database Catan games.
   *
   * @return row ids representing the first turns in all database Catan games.
   */
  @GetMapping(path = "firstTurnRowIDs", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<Map<String, Object>> firstTurnRowIDs() {
    return gameStatesDAO.getRowIDsOfFirstTurnInAllGames();
  }
}
