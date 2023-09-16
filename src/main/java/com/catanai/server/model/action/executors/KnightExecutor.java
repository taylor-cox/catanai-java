package com.catanai.server.model.action.executors;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.bank.card.DevelopmentCard;
import com.catanai.server.model.bank.card.ResourceCard;
import com.catanai.server.model.board.graph.Node;
import com.catanai.server.model.player.Player;
import com.catanai.server.model.player.PlayerID;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class which validates and executes play knight action in game of Catan.
 */
public class KnightExecutor implements SpecificActionExecutor {
  private final Game game;

  public KnightExecutor(Game game) {
    this.game = game;
  }

  @Override
  public boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState) {
    if (currentActionState == ActionState.BUSINESS_AS_USUAL || currentActionState == ActionState.ROLL_DICE) {
      return this.playKnight(amd, p);
    } else {
      return false;
    }
  }

  private boolean playKnight(ActionMetadata amd, Player p) {
    int tileIndex = amd.getRelevantMetadata()[0];
    int playerIDToStealFrom = amd.getRelevantMetadata()[1];

    // Cannot play another dev card if one was already played this turn.
    if (p.hasPlayedDevelopmentCardThisTurn()) {
      return false;
    } else if (!p.hasDevelopmentCard(DevelopmentCard.KNIGHT)) {
      return false;
    } else if (amd.getRelevantMetadata()[0] > 18) {
      return false;
    }

    // Get nodes next to the tile
    Player playerToStealFrom = this.game.getPlayerByID(Objects.requireNonNull(PlayerID.valueOf(playerIDToStealFrom)));
    List<Node> nodesOnTileWithBuildingsFromPlayerToStealFrom = 
        this.game.getBoard().getTiles().get(amd.getRelevantMetadata()[0]).getNodes()
        .stream()
        .filter(node -> node.getBuilding() != null)
        .filter(node -> node.getBuilding().getPlayerId() == playerToStealFrom.getID())
        .collect(Collectors.toList());
    
    if (nodesOnTileWithBuildingsFromPlayerToStealFrom.isEmpty()) {
      return false;
    } else if (!this.game.getBoard().placeRobber(tileIndex)) {
      return false;
    }

    // Steal card from player connected to tile.
    ResourceCard randCardFromHand = playerToStealFrom.takeRandomCardFromHand();
    if (randCardFromHand != null) {
      p.addToResourceCards(randCardFromHand);
    }

    // Remove the knight from the player's hand and set the played dev card flag.
    p.removeDevelopmentCard(DevelopmentCard.KNIGHT);
    p.setPlayedDevelopmentCardThisTurn(true);
    return true;
  }
}
