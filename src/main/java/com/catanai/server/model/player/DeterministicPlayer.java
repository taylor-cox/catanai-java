package com.catanai.server.model.player;

import com.catanai.server.model.gamestate.GameState;
import com.catanai.server.model.player.action.ActionMetadata;

/**
 * Player where each action is decided by an outside source.
 */
public final class DeterministicPlayer extends Player {
  private int[][] nextMoveMetadata;
  private int currentMoveIndex;

  public DeterministicPlayer(PlayerId id) {
    super(id);
    this.currentMoveIndex = 0;
  }

  @Override
  public int[] play(GameState gameState) {
    if (currentMoveIndex > this.nextMoveMetadata.length) {
      return null;
    }
    int oldIndex = currentMoveIndex;
    currentMoveIndex += 1;
    return this.nextMoveMetadata[oldIndex];
  }

  public void setNextMoveMetadata(int[][] metadata) {
    this.nextMoveMetadata = metadata;
  }

  /**
   * Set the next move via metadata.
   *
   * @param metadata metadata refering to next move.
   */
  public void setNextMoveMetadata(ActionMetadata[] metadata) {
    this.nextMoveMetadata = new int[metadata.length][]; 
    for (int i = 0; i < metadata.length; i++) {
      this.nextMoveMetadata[i] = metadata[i].getMetadata();
    }
  }
}
