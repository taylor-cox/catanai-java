import numpy as np
from ml.ppo import Agent
from dao.dao import GameStateDAO
from websockets.game_response_parser import GameResponseParser
from websockets.game_websockets import GameWebSocketHandler
import torch as T

from typing import Dict, List, Tuple

class AgentTrainer:
  def __init__(self, agent_id: str):
    self.N = 55
    self.batch_size = 11
    self.n_epochs = 5
    self.alpha = 0.0003
    self.best_score = 0.0
    self.score_history = []
    self.learn_iters = 0
    self.avg_score = 0.0
    self.n_steps = 0
    self.score: int = 0
    self.starting_turn_counter: int = 0

    self.agent = Agent(
      n_actions=11,
      batch_size=self.batch_size,
      alpha=self.alpha,
      n_epochs=self.n_epochs,
      input_dims=(286, ),
      agent_id=agent_id
    )

class GameTrainer:
  def __init__(self, immediately_run_training: bool = False):
    self.game_state_dao: GameStateDAO = GameStateDAO()
    self.game_websocket_handler: GameWebSocketHandler = GameWebSocketHandler('ws://127.0.0.1:8080/game')
    self.game_response_parser: GameResponseParser = GameResponseParser()
    self._init_training_variables()
    if immediately_run_training:
      self.run_training()
  
  def run_training(self):
    self._run_training_loop()
  
  def _init_training_variables(self):
    self.n_games: int = 10_000
    self.agent_trainers = {
      '1': AgentTrainer('1'),
      '2': AgentTrainer('2'),
      '3': AgentTrainer('3'),
      '4': AgentTrainer('4')
    }
  
  def _run_training_loop(self):
    for i in range(self.n_games):
      with self.game_websocket_handler as game_websocket_handler:
        # Create new game.
        gameStateStr: str = self.game_websocket_handler.newGame()
        self.game_response_parser.setMessage(gameStateStr)
        self.game_state_dao.addGamestate(gameStateStr, i)

        # Get observation of current game state (i.e. 1D array).
        observation: np.ndarray = self.game_response_parser.getGameStateAsObservation()

        # Set starting agent.
        agent_key: str = '1'
        current_agent: AgentTrainer = self.agent_trainers[agent_key]
        done: bool = False

        # First starting turn round robin.
        for i in range(4):
          done, observation = self._run_player_loop(current_agent, game_websocket_handler, observation, agent_key)
          agent_key = str(int(agent_key) + 1)
          if agent_key == '5':
            agent_key = '4'
          current_agent = self.agent_trainers[agent_key]


        # Second starting turn round robin.
        for i in range(4, 0, -1):
          done, observation = self._run_player_loop(current_agent, game_websocket_handler, observation, agent_key)
          agent_key = str(int(agent_key) - 1)
          if agent_key == '0':
            agent_key = '1'
          current_agent = self.agent_trainers[agent_key]

        # Game loop.
        while not done:
          # Player turn loop.
          done, observation = self._run_player_loop(current_agent, game_websocket_handler, observation, agent_key)
          agent_key = str((int(agent_key) % 4) + 1)
          current_agent = self.agent_trainers[agent_key]

        for trainer_id in self.agent_trainers:
          self.agent_trainers[trainer_id].score_history.append(self.agent_trainers[trainer_id].score)
          self.agent_trainers[trainer_id].avg_score = float(np.mean(self.agent_trainers[trainer_id].score_history[-100:]))

          if self.agent_trainers[trainer_id].avg_score > self.agent_trainers[trainer_id].best_score:
            self.agent_trainers[trainer_id].best_score = self.agent_trainers[trainer_id].avg_score
            self.agent_trainers[trainer_id].agent.save_models()
      
      print(f'--------------------------\nEpisode: {i}\n--------------------------')

  def _run_player_loop(
      self,
      current_agent: AgentTrainer,
      game_websocket_handler: GameWebSocketHandler,
      observation: np.ndarray,
      agentKey: str
    ) -> Tuple[bool, np.ndarray]:

    player_done: bool = False
    done: bool = False
    observation = observation
    while not player_done:
      # Have agent choose an action with probs and val.
      action, probs, val = current_agent.agent.choose_action(observation)
      action = [int(x) for x in action.tolist()]
      action[0] += 1

      if current_agent.starting_turn_counter > 5:
        print(f'Player {current_agent.agent.agent_id} attempted move:\n{action}')

      # Make move on backend.
      game_websocket_handler.addMove(action, current_agent.agent.agent_id)
      self.game_response_parser.setMessage(game_websocket_handler.makeMove())

      action[0] -= 1

      # Get dict of response from backend message.
      game_state: dict = self.game_response_parser.getGameState()
      
      # Get the observation of the gamestate after the move.
      observation_ = self.game_response_parser.getGameStateAsObservation()
      reward: float = game_state['reward'][0][0]
      done = game_state['finished'][0][0] == 1

      # Update the number of steps for the agent, their current score, and remember the move + meta.
      current_agent.n_steps += 1
      current_agent.score += int(reward)
      current_agent.agent.remember(observation, action, probs, val, reward, done)

      # If the number of steps is divisible by N, learn.
      if (current_agent.n_steps % current_agent.N) == 0:
        current_agent.agent.learn()
        current_agent.learn_iters += 1

      # Update the observation to the new observation.
      observation = observation_

      if game_state['reward'][0][0] == 1:
        print(f'Player {current_agent.agent.agent_id} successfully played move!')
        print(action)
        self.game_state_dao.addGamestate(self.game_response_parser.getMessage(), current_agent.n_steps)
        current_agent.starting_turn_counter += 1
      
      if (current_agent.starting_turn_counter == 2 \
          or current_agent.starting_turn_counter == 5):
        current_agent.starting_turn_counter += 1
        player_done = True
      
      if action[0] == 13 and current_agent.starting_turn_counter > 5:
        player_done = True
      
    return done, observation

