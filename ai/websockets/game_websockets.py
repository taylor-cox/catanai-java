import websocket
import json
from typing import List

class GameWebSocketHandler:
  """Handler with context manager for handling websocket connection to game,
  getting Gamestates, doing player actions, etc. from backend (Java)."""

  connectionString: str

  def __init__(self, connectionString: str | None = None):
    if connectionString is None:
      self.connectionString = 'ws://127.0.0.1:8080/game'
    else: 
      self.connectionString = connectionString

  def newGame(self) -> str:
    actionString = json.dumps({'command': 'newGame'})
    self.ws.send(bytes(actionString, "utf-8"))
    return self.ws.recv()

  def getCurrentGamestate(self) -> str:
    actionString = json.dumps({'command': 'getCurrentGamestate'})
    self.ws.send(bytes(actionString, "utf-8"))
    return self.ws.recv()

  def makeMove(self) -> str:
    actionString = json.dumps({'command': 'makeMove'})
    self.ws.send(bytes(actionString, "utf-8"))
    return self.ws.recv()

  def addMove(self, action: List[int], playerID: str) -> str:
    actionString = json.dumps(
      {
        'command': 'addPlayerMove',
        'action': action,
        "playerID": playerID
      }
    )
    self.ws.send(bytes(actionString, "utf-8"))
    return self.ws.recv()

  def __enter__(self):
    self.ws = websocket.create_connection(self.connectionString)
    return self
  
  def __exit__(self, type, value, traceback):
    self.ws.close()
  
  def open(self):
    self.ws = websocket.create_connection(self.connectionString)
    return self
  
  def close(self):
    self.ws.close()