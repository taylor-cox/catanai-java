import json
import numpy as np
from typing import List
from itertools import chain
from pydantic.tools import parse_obj_as

from ai.websockets.game_state import GameState

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
    return np.array(
      list(
        chain.from_iterable(
          chain.from_iterable(
            [
              self.game_state.tiles,
              self.game_state.playerPerspectiveResourceCards,
              self.game_state.edges,
              self.game_state.nodes,
              self.game_state.ports,
              self.game_state.playerMetadata,
              self.game_state.lastRoll,
              self.game_state.actionState
            ]
          )
        )
      )
    )