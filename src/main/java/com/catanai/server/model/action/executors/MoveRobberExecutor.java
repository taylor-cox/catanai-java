package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.graph.Node;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.PlayerID;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class which validates and executes move robber action in game of Catan.
 */
public class MoveRobberExecutor implements SpecificActionExecutor {
  private final Game game;

  public MoveRobberExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.MOVE_ROBBER) {
      return this.moveRobber(amd, p);
    } else {
      return false;
    }
  }

  private boolean moveRobber(ActionMetadata amd, Player p) {
    int tileIndex = amd.getRelevantMetadata()[0];
    int playerIDToStealFrom = amd.getRelevantMetadata()[1];

    // Cannot play another dev card if one was already played this turn.
    if (tileIndex > 18 || tileIndex < 0) {
      return false;
    }

    // Check that the player ID is valid.
    if (playerIDToStealFrom < 0 || playerIDToStealFrom > 4) {
      return false;
    }

    Player playerToStealFrom = null;

    // Get nodes next to the tile
    if (playerIDToStealFrom != 0) {
      playerToStealFrom = this.game.getPlayerByID(Objects.requireNonNull(PlayerID.valueOf(playerIDToStealFrom)));
      PlayerID playerToStealFromID = playerToStealFrom.getID();
      List<Node> nodesOnTileWithBuildingsFromPlayerToStealFrom = 
          this.game.getBoard().getTiles().get(amd.getRelevantMetadata()[0]).getNodes()
          .stream()
          .filter(node -> node.getBuilding() != null)
          .filter(node -> node.getBuilding().getPlayerId() == playerToStealFromID)
          .collect(Collectors.toList());
      if (nodesOnTileWithBuildingsFromPlayerToStealFrom.isEmpty()) {
        return false;
      }
    }

    // Check that the robber is placed.
    if (!this.game.getBoard().placeRobber(tileIndex)) {
      return false;
    }

    // Steal card from player connected to tile.
    if (playerToStealFrom != null) {
      ResourceCard randCardFromHand = playerToStealFrom.takeRandomCardFromHand();
      if (randCardFromHand != null) {
        p.addToResourceCards(randCardFromHand);
      }
    }

    return true;
  }
}
