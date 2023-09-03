import json
import numpy as np
from typing import List
from itertools import chain

class GameResponseParser:
  ''' Dictionary respresenting current game state. '''
  game_state: dict

  def __init__(self, message: str = ''):
    if message != '':
      self.setMessage(message)
    else:
      self.game_state = {}
  
  def setMessage(self, message: str):
    self.game_state = json.loads(message)
  
  def getGameState(self) -> dict:
    return self.game_state

  def getGameStateAsObservation(self) -> np.ndarray:
    if self.game_state == {}:
      raise Exception('Game state is empty.')
    return np.array(
      list(
        chain.from_iterable(
          chain.from_iterable(
            [
              self.game_state[key] 
              for key 
              in self.game_state 
              if key != 'playerFullResourceCards' \
                and key != 'actionID' \
                and key != 'currentPlayer' \
                and key != 'orderOfCards' \
                and key != 'reward' \
                and key != 'finished'
            ]
          )
        )
      )
    )