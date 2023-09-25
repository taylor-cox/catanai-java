package com.catanai.server.model.action;

import com.catanai.server.model.Game;
import com.catanai.server.model.action.executors.AcceptTradeExecutor;
import com.catanai.server.model.action.executors.CityExecutor;
import com.catanai.server.model.action.executors.DeclineTradeExecutor;
import com.catanai.server.model.action.executors.DiscardExecutor;
import com.catanai.server.model.action.executors.DrawDevelopmentCardExecutor;
import com.catanai.server.model.action.executors.EndTurnExecutor;
import com.catanai.server.model.action.executors.KnightExecutor;
import com.catanai.server.model.action.executors.MonopolyExecutor;
import com.catanai.server.model.action.executors.MoveRobberExecutor;
import com.catanai.server.model.action.executors.OfferTradeExecutor;
import com.catanai.server.model.action.executors.RoadBuildingExecutor;
import com.catanai.server.model.action.executors.RoadExecutor;
import com.catanai.server.model.action.executors.RollDiceExecutor;
import com.catanai.server.model.action.executors.SettlementExecutor;
import com.catanai.server.model.action.executors.SpecificActionExecutor;
import com.catanai.server.model.action.executors.YearOfPlentyExecutor;
import com.catanai.server.model.player.Player;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
* Class which executes the actions of a player on a game of Catan.
*/
public final class ActionExecutor {
  private final Game game;
  @Getter
  private ActionMetadata lastActionMetadata;
  @Getter
  private final ActionStateMachine actionStateMachine;

  private Map<Action, SpecificActionExecutor> actionToExecutorMap;
  
  /**
   * Generate an ActionExecutor working on a game @param game.
   *
   * @param game game to execute actions on.
   */
  public ActionExecutor(Game game) {
    this.game = game;
    this.actionStateMachine = new ActionStateMachine(game);
    this.initializeActionToExecutorMap();
  }

  private void initializeActionToExecutorMap() {
    // Initialize action executors.
    this.actionToExecutorMap = new HashMap<>();
    this.actionToExecutorMap.put(Action.ACCEPT_TRADE, new AcceptTradeExecutor(this.game));
    this.actionToExecutorMap.put(Action.PLAY_CITY, new CityExecutor(this.game));
    this.actionToExecutorMap.put(Action.DECLINE_TRADE, new DeclineTradeExecutor(this.game));
    this.actionToExecutorMap.put(Action.DISCARD, new DiscardExecutor(this.game));
    this.actionToExecutorMap.put(Action.DRAW_DEVELOPMENT_CARD, new DrawDevelopmentCardExecutor(this.game));
    this.actionToExecutorMap.put(Action.END_TURN, new EndTurnExecutor(this.game));
    this.actionToExecutorMap.put(Action.PLAY_KNIGHT, new KnightExecutor(this.game));
    this.actionToExecutorMap.put(Action.PLAY_MONOPOLY, new MonopolyExecutor(this.game));
    this.actionToExecutorMap.put(Action.MOVE_ROBBER, new MoveRobberExecutor(this.game));
    this.actionToExecutorMap.put(Action.OFFER_TRADE, new OfferTradeExecutor(this.game));
    this.actionToExecutorMap.put(Action.PLAY_ROAD_BUILDING, new RoadBuildingExecutor(this.game));
    this.actionToExecutorMap.put(Action.PLAY_ROAD, new RoadExecutor(this.game));
    this.actionToExecutorMap.put(Action.ROLL_DICE, new RollDiceExecutor(this.game));
    this.actionToExecutorMap.put(Action.PLAY_SETTLEMENT, new SettlementExecutor(this.game));
    this.actionToExecutorMap.put(Action.PLAY_YEAR_OF_PLENTY, new YearOfPlentyExecutor(this.game));
  }

  /**
  * Does the action on the board if possible.
  *
  * @param amd the metadata pertaining to the action
  * @return whether the action was successful or not.
  */
  public boolean doAction(ActionMetadata amd, Player p) {
    this.lastActionMetadata = amd;

    SpecificActionExecutor executor = this.actionToExecutorMap.get(amd.getAction());
    boolean successful = executor.execute(amd, p, this.actionStateMachine.getCurrentActionState());

    if (successful) {
      this.actionStateMachine.nextActionState();
    }
    return successful;
  }
}
