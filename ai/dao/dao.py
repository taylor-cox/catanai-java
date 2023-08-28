from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.engine import URL
from dao.models import GameState
import json

class GameStateDAO:
  def __init__(self):
    self.url = URL.create(
      drivername="postgresql",
      username="postgres",
      password="docker",
      host="localhost",
      database="catan_ai"
    )
    self.recreate_session()
  
  def recreate_session(self):
    self.engine = create_engine(self.url)
    self.Session = sessionmaker(bind=self.engine)
    self.s = self.Session()
  
  def addGamestate(self, gamestate: str, game_id: int):
    self.recreate_session()
    gamestateDict = json.loads(gamestate)
    gamestateToAdd = GameState(
      game_id=game_id,
      tiles = gamestateDict['tiles'],
      playerMetadata = gamestateDict['playerMetadata'],
      nodes = gamestateDict['nodes'],
      banks = gamestateDict['banks'][0],
      edges = gamestateDict['edges'][0],
      playerFullResourceCards = gamestateDict['playerFullResourceCards'],
      playerPerspectiveResourceCards = gamestateDict['playerPerspectiveResourceCards'],
      ports = gamestateDict['ports'][0],
      lastRoll = gamestateDict['lastRoll'][0][0],
      currentPlayer = gamestateDict['currentPlayer'][0][0]
    )
    self.s.add(gamestateToAdd)
    self.s.commit()
    self.s.close()
