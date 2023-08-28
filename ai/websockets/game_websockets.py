import websocket
import json
import time

class GameWebSocketHandler:
  """Handler with context manager for handling websocket connection to game,
  getting Gamestates, doing player actions, etc. from backend (Java)."""

  def __init__(self, connectionString: str):
    self.connectionString = connectionString

  def runAction(self, action: dict) -> str:
    actionString = json.dumps(action)
    self.ws.send(bytes(actionString, "utf-8"))
    return self.ws.recv()

  def __enter__(self):
    self.ws = websocket.create_connection(self.connectionString)
    return self
  
  def __exit__(self, type, value, traceback):
    self.ws.close()