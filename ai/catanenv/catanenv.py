import functools
from gym.spaces import MultiDiscrete, Space
# from gym.spaces.utils import flatten_space
from pettingzoo import AECEnv
# from gym.spaces import Discrete, Space
import numpy as np
from typing import Tuple, TypeVar, List

# Catan imports
from dao.dao import GameStateDAO
from websockets.game_state import GameState
from websockets.game_websockets import GameWebSocketHandler
from websockets.game_response_parser import GameResponseParser

AgentID = TypeVar("AgentID")


class CatanEnv(AECEnv):
    def __init__(self, agent_type: str):
        # Initialize custom classes for interacting with the environment,
        # database, and websockets.
        self.game_state_dao: GameStateDAO = GameStateDAO()
        self.game_websocket_handler: GameWebSocketHandler = GameWebSocketHandler(
            'ws://127.0.0.1:8080/game'
        ).open()
        self.game_response_parser: GameResponseParser = GameResponseParser()

        # Class specific metadata.
        self.agent_type = agent_type
        self.MAX_TURNS = 5_000
        self.num_turns = 0
        self.possible_agents: List[str] = ['1', '2', '3', '4']
        self.agent_selection = self.possible_agents[0]
        self.game_id = self._get_largest_game_id() + 1
        self.num_actions_until_success = 0
        self.agents = self.possible_agents
        self._cumulative_rewards = {
            agent: 0 for agent in self.agents
        }

        # Initialize the environment.
        self.current_observation: np.ndarray = self.game_response_parser.setMessage(
            self.game_websocket_handler.newGame()
        ).getGameStateAsObservation()

        # Add initial gamestate to the database.
        self.game_state_dao.addGamestate(
            self.game_response_parser.game_state_message,
            self.game_id,
            0,
            0,
            [0] * 10,
            agent=self.agent_type
        )

        # Set previous game state to new game
        self.previous_game_state = self.game_response_parser.getGameState()

    def reset(self, seed=None, options=None):
        # Get the next game ID.
        self.game_id = self._get_largest_game_id() + 1

        # Reset websocket connection.
        self.game_websocket_handler.close()
        self.game_websocket_handler.open()

        # Initialize new environment.
        self.current_observation: np.ndarray = self.game_response_parser.setMessage(
            self.game_websocket_handler.newGame()
        ).getGameStateAsObservation()

        # Add initial gamestate to the database.
        self.game_state_dao.addGamestate(
            self.game_response_parser.game_state_message,
            self.game_id,
            0,
            0,
            [0] * 10,
            agent=self.agent_type
        )

        self._cumulative_rewards = {
            agent: 0 for agent in self.agents
        }
        self.terminations = {
            agent: False for agent in self.agents
        }
        self.truncations = {
            agent: False for agent in self.agents
        }
        self.rewards = {
            agent: 0 for agent in self.agents
        }
        self.infos = {
            agent: {} for agent in self.agents
        }

    def observe(self, agent: str):
        return self.current_observation

    def step(self, action):
        # Convert action into something usable
        newAction = []
        categories = [15, 87, 159, 169, 179, 189, 199, 209, 219, 229, 239]
        prev_category = 0
        for category in categories:
            newAction.append(np.argmax(action[prev_category:category]))
        # Make move on backend.
        game_state, observation_, successful, done = self._make_move(
            action, self.agent_selection, self.game_websocket_handler
        )
        reward = self._calculate_reward(
            self.agent_selection,
            successful,
            game_state,
            self.previous_game_state,
            action,
            done,
            self.num_actions_until_success
        )
        self.previous_game_state = game_state
        # Print if the move was successful.
        if game_state.successful[0][0] == 1:
            self.game_state_dao.addGamestate(
                self.game_response_parser.getGameStateMessage(),
                self.game_id,
                self.num_actions_until_success,
                reward,
                action[1:].tolist(),
                self.agent_type
            )
            self.num_actions_until_success = 0
        else:
            self.num_actions_until_success += 1
        self.num_turns += 1

        self.agent_selection = str(
            self.game_response_parser.getGameState().currentPlayer[0][0]
        )

        self._cumulative_rewards[self.agent_selection] += reward
        self.rewards[self.agent_selection] = reward
        self.terminations[self.agent_selection] = done

        if self.num_turns > self.MAX_TURNS:
            done = True
        self.terminations[self.agent_selection] = done

    def close(self):
        self.game_websocket_handler.close()

    @functools.lru_cache(maxsize=None)
    def action_space(self, agent: str) -> Space:
        action_space = MultiDiscrete(
            [15, 72, 72, 10, 10, 10, 10, 10, 10, 10, 10], dtype=np.int64
        )

        return action_space

    @functools.lru_cache(maxsize=None)
    def observation_space(self, agent: str) -> Space:
        return MultiDiscrete(
            [
                entry
                for sublist
                in [
                    [6, 13] * 19,  # Tiles
                    [5, 3] * 54,  # Nodes
                    [5] * 72,  # Edges
                    [7] * 9,  # Ports
                    [20, 20, 20, 20, 20] * 4,  # Player presp. res. cards
                    [16, 16, 16, 16, 16, 16, 16, 16] * 4,  # Player metadata
                    [13],  # Last roll
                    [8],  # Action State
                    [10, 10, 10, 10, 10],  # Development Cards
                    [8],  # Action State
                    [19],  # Robber Index
                ]
                for entry
                in sublist
            ]
        )

    def render(self):
        '''
        Does not render. This is a headless environment.
        '''
        pass

    def _get_largest_game_id(self) -> int:
        return self.game_state_dao.getLargestGameID()

    def _make_move(
        self,
        action: np.ndarray,
        current_agent: str,
        game_websocket_handler: GameWebSocketHandler
    ) -> Tuple[GameState, np.ndarray, float, bool]:
        current_action = action.copy()
        current_action[0] += 1
        current_action = current_action.tolist()

        # Make move on backend.
        game_websocket_handler.addMove(current_action, current_agent)
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

    def _calculate_reward(
        self,
        agent_trainer: str,
        successful: float,
        game_state: GameState,
        previous_game_state: GameState | None,
        action: List[int],
        done: bool,
        failed_actions: int
    ) -> float:
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
            previous_roads: int = previous_game_state.playerMetadata[
                int(agent_trainer) - 1
            ][5]
            current_roads: int = game_state.playerMetadata[
                int(agent_trainer) - 1
            ][5]

            if current_roads > previous_roads:
                reward += 10.0

            previous_settlements: int = previous_game_state.playerMetadata[
                int(agent_trainer) - 1
            ][3]
            current_settlements: int = game_state.playerMetadata[
                int(agent_trainer) - 1
            ][3]

            if current_settlements > previous_settlements:
                reward += 100.0

            previous_cities: int = previous_game_state.playerMetadata[
                int(agent_trainer) - 1
            ][4]
            current_cities: int = game_state.playerMetadata[
                int(agent_trainer) - 1
            ][4]

            if current_cities > previous_cities:
                reward += 150.0

            reward += game_state.playerMetadata[
                int(agent_trainer) - 1
            ][0]

        current_victory_points: int = game_state.playerMetadata[
            int(agent_trainer) - 1
        ][0]
        if current_victory_points >= 10:
            reward += 100_000.0
        reward += (current_victory_points) * 1000.0  # Victory points

        if done:
            reward += 100.0

        reward -= failed_actions * 0.1

        return reward
