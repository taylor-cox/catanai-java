package com.catanai.server;

import com.catanai.server.model.Game;
import com.catanai.server.model.board.graph.Node;
import com.catanai.server.model.gamestate.GameState;
import com.catanai.server.model.player.DeterministicPlayer;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.PlayerId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Main endpoint for Catan AI webviewer.
 */
@Controller
@RequestMapping("api/v1")
@CrossOrigin(origins = "*")
public class Main {
  /**
   * Returns random game with starting moves determined. TODO: change this.
   *
   * @param model ???
   * @return gamestatew ith one player move
   */
  @GetMapping(path = "/randomGame", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public GameState randomGame() {
    List<Player> players = new ArrayList<Player>(4);
    players.add(new DeterministicPlayer(PlayerId.ONE));
    players.add(new DeterministicPlayer(PlayerId.TWO));
    players.add(new DeterministicPlayer(PlayerId.THREE));
    players.add(new DeterministicPlayer(PlayerId.FOUR));

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
      ((DeterministicPlayer) p).setNextMoveMetadata(setMoves);
      counter += 1;
    }

    Game game = new Game(players);

    game.startingTurns();

    return game.getCurrentGameState();
  }

  @GetMapping(path = "/nodeEdgeMappings", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ArrayList<Node> nodeTileMappings() {
    return null;
  }
}
