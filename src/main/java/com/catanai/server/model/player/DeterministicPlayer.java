package com.catanai.server.model.player;

import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.gamestate.GameState;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Player where each action is decided by an outside source.
 */
public final class DeterministicPlayer extends Player {
  private Queue<int[]> moveMetadatas;

  public DeterministicPlayer(PlayerID id) {
    super(id);
    this.moveMetadatas = new LinkedList<int[]>();
  }

  @Override
  public int[] play(GameState gameState) {
    // int[] move = this.moveMetadatas.poll();
    int[] move = this.moveMetadatas.remove();
    ActionMetadata metadata = new ActionMetadata(move);
    this.previousAction = metadata;
    return move;
  }

  public void addNextMove(int[] move) {
    this.moveMetadatas.add(move);
  }

  public void addAllMoves(int[][] moves) {
    this.moveMetadatas.addAll(Arrays.asList(moves));
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

  public int[] getNextMoveMetadata() {
    return this.moveMetadatas.peek();
  }
}
