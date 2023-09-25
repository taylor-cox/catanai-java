from pydantic.dataclasses import dataclass
from typing import List, Optional

@dataclass
class GameState(object):
  tiles: List[List[int]]
  playerPerspectiveResourceCards: List[List[int]]
  playerFullResourceCards: List[List[int]]
  edges: List[List[int]]
  nodes: List[List[int]]
  ports: List[List[int]]
  lastRoll: List[List[int]]
  currentPlayer: List[List[int]]
  actionID: List[List[int]]
  finished: List[List[int]]
  actionState: List[List[int]]
  successful: List[List[int]]
  playerMetadata: List[List[int]]