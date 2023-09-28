import numpy as np
from websockets.game_state import GameState
from ml.fully_connected.fully_connected_ppo import FullyConnectedAgent
from dao.dao import GameStateDAO
from websockets.game_response_parser import GameResponseParser
from websockets.game_websockets import GameWebSocketHandler
import torch as T
from tqdm import tqdm
import time
import logging

from typing import List, Tuple

logging.basicConfig(filename='game_trainer.log', encoding='utf-8', level=logging.INFO)

class AgentTrainer:
    def __init__(self, player_id: str):
        self.N: int = 512
        self.batch_size: int = 32
        self.n_epochs: int = 8
        self.alpha: float = 0.0003
        self.best_score: float = 0.0
        self.score_history: List[float] = []
        self.learn_iters: int = 0
        self.avg_score: float = 0.0
        self.n_steps: int = 0
        self.score: float = 0
        self.starting_turn_counter: int = 0
        self.player_id: str = player_id

        self.agent = FullyConnectedAgent(
            n_actions=11,
            batch_size=self.batch_size,
            alpha=self.alpha,
            n_epochs=self.n_epochs,
            input_dims=(286,)
        )
        self.load_models()

    def reset(self):
        self.starting_turn_counter = 0
    
    def save_models(self):
        logging.info('Saving models.')
        self.agent.save_models(player_id=self.player_id)
    
    def load_models(self):
        logging.info('Loading models.')
        self.agent.load_models(player_id=self.player_id)

class GameTrainer:
    def __init__(self, immediately_run_training: bool = False, profiling: bool = False):
        self.game_state_dao: GameStateDAO = GameStateDAO()
        self.game_websocket_handler: GameWebSocketHandler = GameWebSocketHandler('ws://127.0.0.1:8080/game')
        self.game_response_parser: GameResponseParser = GameResponseParser()

        # Set up agent trainers.
        self.current_game_number: int = 0
        self.profiling: bool = profiling
        self.N_GAMES: int = 10_000 if not profiling else 1
        # self.agent_trainer = AgentTrainer()
        self.n_turns = 0
        self.MAX_TURNS = 5_000
        self.agent_trainers = {
            '1': AgentTrainer('1'),
            '2': AgentTrainer('2'),
            '3': AgentTrainer('3'),
            '4': AgentTrainer('4')
        }

        # Start training if immediately_run_training is True.
        if immediately_run_training:
            self.run_training()

    def run_training(self):
        self._run_training_loop()

    def _run_training_loop(self):
        try:
            largest_game_id: int = self.game_state_dao.getLargestGameID()
            for i in range(largest_game_id + 1 if largest_game_id != -1 else 0, self.N_GAMES if not self.profiling else largest_game_id + 2):
                logging.info(f'-------------------------- Episode: {i} --------------------------')
                time_start = time.perf_counter()
                print(f'-------------------------- Episode: {i} --------------------------')
                self.current_game_number = i
                # Open the websocket connection.
                with self.game_websocket_handler as game_websocket_handler:
                    # Create new game, and get the observation of the current game state.
                    self._run_game_loop(game_websocket_handler)
                # After the game is over, reset the agent trainer and other vars per game.
                time_end = time.perf_counter()
                logging.info(f'Episode length (time): {((time_end - time_start) // 60) // 60}:{((time_end - time_start) // 60) % 60}:{(time_start - time_end) % 60}')
                for agent_trainer_key in self.agent_trainers.keys():
                    self.agent_trainers[agent_trainer_key].reset()
                self.n_turns = 0
        except Exception as e:
            print(e)
            print('ERROR OCCURRED: Waiting for 10 seconds to restart training.')
            logging.error(f"ERROR OCCURRED at epoch number {self.game_state_dao.getLargestGameID()}: Waiting for 10 seconds to restart training.")
            time.sleep(10)
            for agent_trainer_key in self.agent_trainers.keys():
                self.agent_trainers[agent_trainer_key].reset()
            self._run_training_loop()

    def _run_game_loop(self, game_websocket_handler: GameWebSocketHandler):
        observation: np.ndarray = self._new_game(game_websocket_handler)
        done: bool = False
        num_actions_until_success: int = 0
        previous_game_state: GameState | None = None
        current_agent_trainer: AgentTrainer = self.agent_trainers['1']
        for _ in range(10000 if not self.profiling else 1025):
            if done or self.n_turns >= self.MAX_TURNS:
                break
            # Have agent choose an action with probs and val.
            with T.no_grad():
                action, probs, val = current_agent_trainer.agent.choose_action(observation)
            # Convert action to list of ints.
            action = [int(x) for x in action.tolist()]

            # Make the move on the backend, and get the game state.
            game_state: GameState
            observation_: np.ndarray
            reward: float
            successful: float
            done: bool
            game_state, observation_, successful, done = self._make_move(action, current_agent_trainer, game_websocket_handler)
            reward = self._calculate_reward(current_agent_trainer, successful, game_state, previous_game_state, action, done, num_actions_until_success)
            previous_game_state = game_state

            # Update agent metadata, and learn if necessary.
            self._update_agent(current_agent_trainer, observation, action, probs, val, reward, done)

            # Update the observation to the new observation.
            observation = observation_

            # Print if the move was successful.
            if game_state.successful[0][0] == 1:
                # print(f'Player {self.agent_trainer.player_id} successfully played move after {num_actions_until_success} failed actions.')
                # print(action)
                if not self.profiling:
                    self.game_state_dao.addGamestate(self.game_response_parser.getGameStateMessage(), self.current_game_number, num_actions_until_success, reward, action[1:])
                num_actions_until_success = 0
            else:
                num_actions_until_success += 1

            # Update agent to act as next player
            old_agent_id: str = current_agent_trainer.player_id
            current_agent_trainer = self.agent_trainers[self._get_next_player()]
            if old_agent_id != current_agent_trainer.player_id:
                self.n_turns += 1

        # Update the score history and avg score.
        for trainer_key in self.agent_trainers.keys():
            self._update_agent_trainer_scores(self.agent_trainers[trainer_key])

    def _update_agent_trainer_scores(self, agent_trainer: AgentTrainer):
        agent_trainer.score_history.append(agent_trainer.score)
        agent_trainer.avg_score = float(np.mean(agent_trainer.score_history[-100:]))
        if agent_trainer.avg_score > agent_trainer.best_score:
            agent_trainer.best_score = agent_trainer.avg_score
            agent_trainer.save_models()

    def _new_game(self, game_websocket_handler: GameWebSocketHandler) -> np.ndarray:
        game_state_str: str = game_websocket_handler.newGame()
        self.game_response_parser.setMessage(game_state_str)
        if not self.profiling:
            self.game_state_dao.addGamestate(game_state_str, self.current_game_number, 0, 0, [0] * 10)
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
        current_agent.agent.remember(observation, action, probs, val, reward, int(done))

        # If the number of steps is divisible by N, learn.
        if (current_agent.n_steps % current_agent.N) == 0:
            current_agent.agent.learn()
            current_agent.save_models()
            current_agent.learn_iters += 1

    def _calculate_reward(self, agent_trainer: AgentTrainer, successful: float, game_state: GameState, previous_game_state: GameState | None, action: List[int], done: bool, failed_actions: int) -> float:
        reward: float = 0.0
        # If the move was successful, add 1 to the reward. Otherwise, subtract 100.
        if successful == 1:
            if action[0] == 13 or action[0] == 14:
                return 0.1
            else:
                reward += 100.0
        else:
            return -100.0

        # If the player gained a victory point, add 10 to the reward for each victory point.
        if previous_game_state is not None:
            previous_victory_points: int = previous_game_state.playerMetadata[int(agent_trainer.player_id) - 1][0]
            current_victory_points: int = game_state.playerMetadata[int(agent_trainer.player_id) - 1][0]
            if current_victory_points >= 10:
                reward += 100_000.0
            
            previous_roads: int = previous_game_state.playerMetadata[int(agent_trainer.player_id) - 1][5]
            current_roads: int = game_state.playerMetadata[int(agent_trainer.player_id) - 1][5]

            if current_roads > previous_roads:
                reward += 10.0
            
            previous_settlements: int = previous_game_state.playerMetadata[int(agent_trainer.player_id) - 1][3]
            current_settlements: int = game_state.playerMetadata[int(agent_trainer.player_id) - 1][3]

            if current_settlements > previous_settlements:
                reward += 100.0
            
            previous_cities: int = previous_game_state.playerMetadata[int(agent_trainer.player_id) - 1][4]
            current_cities: int = game_state.playerMetadata[int(agent_trainer.player_id) - 1][4]

            if current_cities > previous_cities:
                reward += 150.0
            
            reward += game_state.playerMetadata[int(agent_trainer.player_id) - 1][0]
            reward += (current_victory_points - previous_victory_points) * 1000.0 # Victory points
            
        
        if done:
            reward += 100.0
        
        reward -= failed_actions * 0.1

        return reward