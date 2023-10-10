import numpy as np
from .agent_trainer import AgentTrainer
from websockets.game_state import GameState
from dao.dao import GameStateDAO
from websockets.game_response_parser import GameResponseParser
from websockets.game_websockets import GameWebSocketHandler
import torch as T
import time
import logging
from typing import Dict, List, Tuple
from tqdm import tqdm
# from pettingzoo import AECEnv
# from copy import copy
# import functools
# from gymnasium.spaces import Discrete, MultiDiscrete


class GameTrainer:
    def __init__(self, immediately_run_training: bool = False, profiling: bool = False):
        self.game_state_dao: GameStateDAO = GameStateDAO()
        self.game_websocket_handler: GameWebSocketHandler = GameWebSocketHandler(
            'ws://127.0.0.1:8080/game')
        self.game_response_parser: GameResponseParser = GameResponseParser()
        self.N_GAMES = 10_000
        self.N_GAME_ITERATIONS = 10_000
        self.profiling = profiling
        self.agent_type = 'DQN'

        # Set up agent trainers.
        self.current_game_number: int = 0
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
        self.largest_game_id: int = self.game_state_dao.getLargestGameID()
        for i in range(self.largest_game_id + 1 if self.largest_game_id != None else 0, self.N_GAMES if not self.profiling else self.largest_game_id + 2):
            logging.info(
                f'-------------------------- Episode: {i} --------------------------')
            time_start = time.perf_counter()
            print(
                f'-------------------------- Episode: {i} --------------------------')
            self.current_game_number = i
            # Open the websocket connection.
            with self.game_websocket_handler as game_websocket_handler:
                # Create new game, and get the observation of the current game state.
                self._run_game_loop(game_websocket_handler)
            # After the game is over, reset the agent trainer and other vars per game.
            time_end = time.perf_counter()
            logging.info(
                f'Episode length (time): {((time_end - time_start) // 60) // 60}:{((time_end - time_start) // 60) % 60}:{(time_start - time_end) % 60}')
            for agent_trainer_key in self.agent_trainers.keys():
                self.agent_trainers[agent_trainer_key].reset()
            self.n_turns = 0

    def _run_game_loop(self, game_websocket_handler: GameWebSocketHandler):
        observation: np.ndarray = self._new_game(game_websocket_handler)
        done: bool = False
        num_actions_until_success: int = 0
        previous_game_state: GameState | None = None
        current_agent_trainer: AgentTrainer = self.agent_trainers['1']
        for i in tqdm(range(self.N_GAME_ITERATIONS)):
            if done or self.n_turns >= self.MAX_TURNS:
                break
            # Have agent choose an action with probs and val.
            with T.no_grad():
                action = current_agent_trainer.agent.choose_action(observation)
            # Convert action to list of ints.
            action = [int(x) for x in action]

            # Make the move on the backend, and get the game state.
            game_state: GameState
            observation_: np.ndarray
            reward: float
            successful: float
            done: bool
            game_state, observation_, successful, done = self._make_move(
                action, current_agent_trainer, game_websocket_handler)
            reward = self._calculate_reward(
                current_agent_trainer, successful, game_state, previous_game_state, action, done, num_actions_until_success, i)
            previous_game_state = game_state

            # Update agent metadata, and learn if necessary.
            action_lengths = [15, 72, 72, 10, 10, 10, 10, 10, 10, 10, 10]
            real_actions = []
            for i, action_length in enumerate(action_lengths):
                for j in range(action_length):
                    real_actions.append(1 if j == action[i] else 0)

            self._update_agent(current_agent_trainer, observation,
                               real_actions, reward, observation_, done)

            # Update the observation to the new observation.
            observation = observation_

            # Print if the move was successful.
            if game_state.successful[0][0] == 1:
                # print(f'Player {self.agent_trainer.player_id} successfully played move after {num_actions_until_success} failed actions.')
                # print(action)
                if not self.profiling:
                    self.game_state_dao.addGamestate(self.game_response_parser.getGameStateMessage(
                    ), self.current_game_number, num_actions_until_success, reward, action[1:], self.agent_type)
                num_actions_until_success = 0
            else:
                num_actions_until_success += 1

            # Update agent to act as next player
            old_agent_id: str = current_agent_trainer.player_id
            current_agent_trainer = self.agent_trainers[
                self._get_next_player()
            ]
            if old_agent_id != current_agent_trainer.player_id:
                self.n_turns += 1

            if done:
                break

        # Update the score history and avg score.
        for trainer_key in self.agent_trainers.keys():
            self._update_agent_trainer_scores(self.agent_trainers[trainer_key])

    def _update_agent_trainer_scores(self, agent_trainer: AgentTrainer):
        agent_trainer.score_history.append(agent_trainer.score)
        agent_trainer.avg_score = float(
            np.mean(agent_trainer.score_history[-100:]))
        if agent_trainer.avg_score > agent_trainer.best_score:
            agent_trainer.best_score = agent_trainer.avg_score
            agent_trainer.save_models()

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

    def _update_agent(self, current_agent: AgentTrainer, observation: np.ndarray, action: List[int], reward: float,
                      observation_: np.ndarray, done: bool):
        # Update the number of steps for the agent, their current score, and remember the move + meta.
        current_agent.n_steps += 1
        current_agent.score += reward
        current_agent.agent.store_transition(
            observation, action, reward, observation_, int(done))

        current_agent.agent.learn()
        current_agent.soft_update_target_network()

    def _calculate_reward(self, agent_trainer: AgentTrainer, successful: float, game_state: GameState, previous_game_state: GameState | None, action: List[int], done: bool, failed_actions: int, i: int) -> float:
        reward: float = 0.0
        # If the move was successful, add 1 to the reward. Otherwise, subtract 100.
        if successful == 1:
            if action[0] == 13 or action[0] == 14:
                return 0.1 - (failed_actions * 0.1)
            else:
                reward += 100.0
        else:
            return -100.0

        # If the player gained a victory point, add 10 to the reward for each victory point.
        if previous_game_state is not None:
            previous_victory_points: int = previous_game_state.playerMetadata[int(
                agent_trainer.player_id) - 1][0]
            current_victory_points: int = game_state.playerMetadata[int(
                agent_trainer.player_id) - 1][0]
            if current_victory_points >= 10:
                reward += 100_000.0

            previous_roads: int = previous_game_state.playerMetadata[int(
                agent_trainer.player_id) - 1][5]
            current_roads: int = game_state.playerMetadata[int(
                agent_trainer.player_id) - 1][5]

            if current_roads > previous_roads:
                reward += 10.0

            previous_settlements: int = previous_game_state.playerMetadata[int(
                agent_trainer.player_id) - 1][3]
            current_settlements: int = game_state.playerMetadata[int(
                agent_trainer.player_id) - 1][3]

            if current_settlements > previous_settlements:
                reward += 100.0

            previous_cities: int = previous_game_state.playerMetadata[int(
                agent_trainer.player_id) - 1][4]
            current_cities: int = game_state.playerMetadata[int(
                agent_trainer.player_id) - 1][4]

            if current_cities > previous_cities:
                reward += 150.0

            reward += game_state.playerMetadata[int(
                agent_trainer.player_id) - 1][0]
            reward += (current_victory_points -
                       previous_victory_points) * 1000.0  # Victory points

        if done:
            reward += 100.0

        reward -= failed_actions * 0.1

        return reward

    def _new_game(self, game_websocket_handler: GameWebSocketHandler) -> np.ndarray:
        game_state_str: str = game_websocket_handler.newGame()
        self.game_response_parser.setMessage(game_state_str)
        self.game_state_dao.addGamestate(
            game_state_str, self.largest_game_id, 0, 0, [0] * 10)
        return self.game_response_parser.getGameStateAsObservation()


# class CatanEnvironment(AECEnv):
#     metadata = {
#         'name': 'catan_environment_v0'
#     }

#     def __init__(self, render_mode = 'database', agents: List[Base_Agent] = []):
#         # TODO: Change this to have 2-4 agents.
#         if len(agents) < 4 or len(agents) > 5:
#             raise Exception('Must have 4 agents for a Catan game.')

#         self.internal_render_mode = None
#         # Game state variables.
#         self.game_state_dao: GameStateDAO = GameStateDAO()
#         self.game_websocket_handler: GameWebSocketHandler = GameWebSocketHandler('ws://127.0.0.1:8080/game').open()
#         self.game_response_parser: GameResponseParser = GameResponseParser()

#         self.prior_observation: np.ndarray = self.game_response_parser.setMessage(
#             self.game_websocket_handler.newGame()
#         ).getGameStateAsObservation()

#         # Agent variables and game metadata.
#         self.game_number: int = 0
#         self.possible_agents: List[str] = [str(i) for i in range(1, 5)]
#         self.agent_id_mapping: Dict[str, Base_Agent] = {str(i+1): agents[i] for i in range(len(agents))}
#         self.current_agent_id: str = '1' # Always start with the first agent.
#         self.game_beginning: bool = True
#         self.last_move_successful: bool = False

#     def reset(self, seed=None, options=None):
#         self.agents = copy(self.possible_agents)
#         # Reset connection.
#         self.game_websocket_handler.close()
#         self.game_websocket_handler.open()
#         # Set the prior obseervation to a new game.
#         self.prior_observation = self._new_game(self.game_websocket_handler)

#     def step(self, action):
#         """
#         step(action) takes in an action for the current agent (specified by
#         agent_selection) and needs to update
#         - rewards
#         - _cumulative_rewards (accumulating the rewards)
#         - terminations
#         - truncations
#         - infos
#         - agent_selection (to the next agent)
#         And any internal state used by observe() or render()
#         """
#         # the agent which stepped last had its _cumulative_rewards accounted for
#         # (because it was returned by last()), so the _cumulative_rewards for this
#         # agent should start again at 0
#         # observation: np.ndarray = self._new_game(self.game_websocket_handler)
#         # done: bool = False
#         # num_actions_until_success: int = 0
#         # previous_game_state: GameState | None = None
#         # current_agent_trainer: AgentTrainer = self.agent_trainers['1']
#         # for _ in range(10000):
#         #     if done or self.n_turns >= self.MAX_TURNS:
#         #         break
#         #     # Have agent choose an action with probs and val.
#         #     with T.no_grad():
#         #         action, probs, val = current_agent_trainer.agent.choose_action(observation)
#         #     # Convert action to list of ints.
#         #     action = [int(x) for x in action.tolist()]

#         #     # Make the move on the backend, and get the game state.
#         #     game_state: GameState
#         #     observation_: np.ndarray
#         #     reward: float
#         #     successful: float
#         #     done: bool
#         #     game_state, observation_, successful, done = self._make_move(action, current_agent_trainer, game_websocket_handler)
#         #     reward = self._calculate_reward(current_agent_trainer, successful, game_state, previous_game_state, action, done, num_actions_until_success)
#         #     previous_game_state = game_state

#         #     # Update agent metadata, and learn if necessary.
#         #     self._update_agent(current_agent_trainer, observation, action, probs, val, reward, done)

#         #     # Update the observation to the new observation.
#         #     observation = observation_

#         #     # Print if the move was successful.
#         #     if game_state.successful[0][0] == 1:
#         #         # print(f'Player {self.agent_trainer.player_id} successfully played move after {num_actions_until_success} failed actions.')
#         #         # print(action)
#         #         self.game_state_dao.addGamestate(self.game_response_parser.getGameStateMessage(), self.current_game_number, num_actions_until_success, reward, action[1:])
#         #         num_actions_until_success = 0
#         #     else:
#         #         num_actions_until_success += 1

#         #     # Update agent to act as next player
#         #     old_agent_id: str = current_agent_trainer.player_id
#         #     current_agent_trainer = self.agent_trainers[self._get_next_player()]
#         #     if old_agent_id != current_agent_trainer.player_id:
#         #         self.n_turns += 1

#         # # Update the score history and avg score.
#         # for trainer_key in self.agent_trainers.keys():
#         #     self._update_agent_trainer_scores(self.agent_trainers[trainer_key])
#         pass

#     @functools.lru_cache(maxsize=None)
#     def observation_space(self, agent):
#         # gymnasium spaces are defined and documented here: https://gymnasium.farama.org/api/spaces/
#         obs_space  = [6, 13] * 19 # populate tiles [terrain, tile chit]
#         obs_space += [5, 3]  * 54 # populate nodes [building player, building type]
#         obs_space += [5]     * 72 # populate edges [road player]
#         obs_space += [7]     * 9  # populate ports [port type]
#         obs_space += [4, 20] * 4  # populate player perspective resource cards [resource type, amount]
#         obs_space += [10, 2, 2, 5, 4, 14, 20, 20] * 4 # populate player metadata [victory points, largest army, longest road, remaining settlements, remaining cities, remaining roads, num knights played, num development cards]
#         obs_space += [13] # populate last roll [roll]
#         obs_space += [10] # populate action_state [action_state]
#         obs_space += [10] * 5 # populate player development cards [num knight, num road building,  num year of plenty, num monopoly, num vp]

#         return MultiDiscrete(obs_space)

#     @functools.lru_cache(maxsize=None)
#     def action_space(self, agent):
#         return MultiDiscrete([
#             16, # Action
#             72, # Possibly holds road index
#             72, # Possibly holds road index
#             10, # Card type amount for trading
#             10, # Card type amount for trading
#             10, # Card type amount for trading
#             10, # Card type amount for trading
#             10, # Card type amount for trading
#             10, # Card type amount for trading
#             10, # Card type amount for trading
#             10  # Card type amount for trading
#         ])
