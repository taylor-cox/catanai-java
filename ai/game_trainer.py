import numpy as np
from ai.websockets.game_state import GameState
from ml.ppo import Agent
from dao.dao import GameStateDAO
from websockets.game_response_parser import GameResponseParser
from websockets.game_websockets import GameWebSocketHandler
import torch as T

from typing import List, Tuple

class AgentTrainer:
    def __init__(self):
        self.N: int = 55
        self.batch_size: int = 11
        self.n_epochs: int = 5
        self.alpha: float = 0.0003
        self.best_score: float = 0.0
        self.score_history: List[float] = []
        self.learn_iters: int = 0
        self.avg_score: float = 0.0
        self.n_steps: int = 0
        self.score: float = 0
        self.starting_turn_counter: int = 0
        self.player_id: str = '1'

        self.agent = Agent(
            n_actions=11,
            batch_size=self.batch_size,
            alpha=self.alpha,
            n_epochs=self.n_epochs,
            input_dims=(286,)
        )


class GameTrainer:
    def __init__(self, immediately_run_training: bool = False):
        self.game_state_dao: GameStateDAO = GameStateDAO()
        self.game_websocket_handler: GameWebSocketHandler = GameWebSocketHandler('ws://127.0.0.1:8080/game')
        self.game_response_parser: GameResponseParser = GameResponseParser()

        # Set up agent trainers.
        self.current_game_number: int = 0
        self.n_games: int = 1
        self.agent_trainer = AgentTrainer()

        # Start training if immediately_run_training is True.
        if immediately_run_training:
            self.run_training()

    def run_training(self):
        self._run_training_loop()

    def _run_training_loop(self):
        for i in range(self.n_games):
            self.current_game_number = i
            # Open the websocket connection.
            with self.game_websocket_handler as game_websocket_handler:
                # Create new game, and get the observation of the current game state.
                self._run_game_loop(game_websocket_handler)

            print(f'--------------------------\nEpisode: {i}\n--------------------------')

    def _run_game_loop(self, game_websocket_handler: GameWebSocketHandler):
        observation: np.ndarray = self._new_game(game_websocket_handler)

        done: bool = False
        while not done:
            # Have agent choose an action with probs and val.
            action, probs, val = self.agent_trainer.agent.choose_action(observation)
            # Convert action to list of ints.
            action = [int(x) for x in action.tolist()]

            # Make the move on the backend, and get the game state.
            game_state: GameState
            observation_: np.ndarray
            reward: float
            done: bool
            game_state, observation_, reward, done = self._make_move(action, self.agent_trainer,
                                                                     game_websocket_handler)

            # Update agent metadata, and learn if necessary.
            self._update_agent(self.agent_trainer, observation, action, probs, val, reward, done)

            # Update the observation to the new observation.
            observation = observation_

            # Print if the move was successful.
            if game_state.successful[0][0] == 1:
                print(f'Player {self.agent_trainer.player_id} successfully played move!')
                print(action)
                self.game_state_dao.addGamestate(self.game_response_parser.getGameStateMessage(),
                                                 self.current_game_number)

            # Update agent to act as next player
            self.agent_trainer.player_id = self._get_next_player()

        # Update the score history and avg score.
        self.agent_trainer.score_history.append(self.agent_trainer.score)
        self.agent_trainer.avg_score = float(np.mean(self.agent_trainer.score_history[-100:]))

        # If the avg score is greater than the best score, save the models.
        if self.agent_trainer.avg_score > self.agent_trainer.best_score:
            self.agent_trainer.best_score = self.agent_trainer.avg_score
            self.agent_trainer.agent.save_models()

    def _new_game(self, game_websocket_handler: GameWebSocketHandler) -> np.ndarray:
        game_state_str: str = game_websocket_handler.newGame()
        self.game_response_parser.setMessage(game_state_str)
        self.game_state_dao.addGamestate(game_state_str, self.current_game_number)
        return self.game_response_parser.getGameStateAsObservation()

    def _make_move(
            self,
            action: List[int],
            current_agent: AgentTrainer,
            game_websocket_handler: GameWebSocketHandler
    ) -> Tuple[GameState, np.ndarray, float, bool]:
        current_action = action.copy()
        current_action[0] += 1

        # Make move on backend.
        game_websocket_handler.addMove(current_action, current_agent.player_id)
        game_state_str: str = game_websocket_handler.makeMove()
        self.game_response_parser.setMessage(game_state_str)
        to_return: Tuple[GameState, np.ndarray, float, bool] = (
            self.game_response_parser.getGameState(),
            self.game_response_parser.getGameStateAsObservation(),
            self.game_response_parser.getSuccessful(),
            self.game_response_parser.getGameDone()
        )
        current_action[0] -= 1
        return to_return

    def _get_next_player(self) -> str:
        return str(self.game_response_parser.getGameState().currentPlayer[0][0])

    def _update_agent(self, current_agent: AgentTrainer, observation: np.ndarray, action: List[int], probs: T.Tensor,
                      val: T.Tensor, reward: float, done: bool):
        # Update the number of steps for the agent, their current score, and remember the move + meta.
        current_agent.n_steps += 1
        current_agent.score += reward
        current_agent.agent.remember(observation, action, probs, val, reward, done)

        # If the number of steps is divisible by N, learn.
        if (current_agent.n_steps % current_agent.N) == 0:
            current_agent.agent.learn()
            current_agent.learn_iters += 1
