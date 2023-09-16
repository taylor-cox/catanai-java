package com.catanai.server.model.action.executors;

import com.catanai.server.model.action.ActionMetadata;
import com.catanai.server.model.action.ActionState;
import com.catanai.server.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Interface representing a specific action executor.
 * Executes a specific action in the game of Catan.
 */
public interface SpecificActionExecutor {
  /**
   * Executes a specific action in the game of Catan.
   *
   * @param amd metadata pertaining to action
   * @param p player attempting to execute action
   * @param currentActionState current action state
   * @return true if action was executed successfully, false otherwise
   */
  boolean execute(@NotNull ActionMetadata amd, @NotNull Player p, @NotNull ActionState currentActionState);
}
