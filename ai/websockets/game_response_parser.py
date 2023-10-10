import json
import numpy as np
from typing import List
from itertools import chain
from pydantic.tools import parse_obj_as
from websockets.game_state import GameState


class GameResponseParser:
    ''' Dictionary respresenting current game state. '''
    game_state: GameState
    game_state_message: str

    def __init__(self, message: str = ''):
        if message != '':
            self.setMessage(message)

    def setMessage(self, message: str):
        self.game_state = parse_obj_as(GameState, json.loads(message))
        self.game_state_message = message
        return self

    def getGameState(self) -> GameState:
        return self.game_state

    def getGameStateMessage(self) -> str:
        return self.game_state_message

    def getSuccessful(self) -> float:
        return self.game_state.successful[0][0]

    def getGameDone(self) -> bool:
        return self.game_state.finished[0][0] == 1

    def getGameStateAsObservation(self) -> np.ndarray:
        if self.game_state == {}:
            raise Exception('Game state is empty.')
        arr = []
        # Add tiles to observation
        for val in self.game_state.tiles:
            for val2 in val:
                arr.append(val2)

        # Add nodes, edges, and ports to observation
        for val in self.game_state.nodes:
            for val2 in val:
                arr.append(val2)

        for val in self.game_state.edges:
            for val2 in val:
                arr.append(val2)

        for val in self.game_state.ports:
            for val2 in val:
                arr.append(val2)

        # Add player data to observation
        for val in self.game_state.playerPerspectiveResourceCards:
            for val2 in val:
                arr.append(val2)

        for val in self.game_state.playerMetadata:
            for val2 in val:
                arr.append(val2)

        for val in self.game_state.lastRoll:
            for val2 in val:
                arr.append(val2)

        for val in self.game_state.actionState:
            for val2 in val:
                arr.append(val2)

        for val in self.game_state.playerDevelopmentCards[int(self.game_state.currentPlayer[0][0]) - 1]:
            arr.append(val)

        arr.append(self.game_state.actionState[0][0])
        arr.append(self.game_state.robberIndex[0][0])

        return np.array(arr)
