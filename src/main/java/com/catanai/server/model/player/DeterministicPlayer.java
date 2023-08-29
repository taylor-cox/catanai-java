package com.catanai.server.model.player;

import com.catanai.server.model.gamestate.GameState;
import com.catanai.server.model.player.action.ActionMetadata;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Player where each action is decided by an outside source.
 */
public final class DeterministicPlayer extends Player {
  private Queue<int[]> moveMetadatas;

  public DeterministicPlayer(PlayerId id) {
    super(id);
    this.moveMetadatas = new LinkedList<int[]>();
  }

  @Override
  public int[] play(GameState gameState) {
    int[] move = this.moveMetadatas.poll();
    return move;
  }

  public void addNextMove(int[] move) {
    this.moveMetadatas.add(move);
  }

  public void addAllMoves(int[][] moves) {
    for (int i = 0; i < moves.length; i++) {
      this.moveMetadatas.add(moves[i]);
    }
  }

  /**
   * Set the next move via metadata.
   *
   * @param metadata metadata refering to next move.
   */
  public void setNextMoveMetadata(ActionMetadata[] metadata) {
    for (int i = 0; i < metadata.length; i++) {
      this.moveMetadatas.add(metadata[i].getMetadata());
    }
  }
}
