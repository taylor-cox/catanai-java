import json
import numpy as np
from typing import List
from itertools import chain

class GameResponseParser:
  ''' Dictionary respresenting current game state. '''
  game_state: dict
  game_state_message: str

  def __init__(self, message: str = ''):
    if message != '':
      self.setMessage(message)
    else:
      self.game_state = {}
  
  def setMessage(self, message: str):
    self.game_state = json.loads(message)
    self.game_state_message = message
  
  def getGameState(self) -> dict:
    return self.game_state

  def getGameStateMessage(self) -> str:
    return self.game_state_message
  
  def getReward(self) -> float:
    return self.game_state['reward'][0][0]
  
  def getGameDone(self) -> bool:
    return self.game_state['finished'][0][0] == 1

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