import websocket
import json
import time

class GameWebSocketHandler:
  """Handler with context manager for handling websocket connection to game,
  getting Gamestates, doing player actions, etc. from backend (Java)."""

  def __init__(self, connectionString: str):
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

  def addMove(self, action: list, playerID: str) -> str:
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