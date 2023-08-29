from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, ARRAY, BigInteger, SmallInteger

Base = declarative_base()

class GameState(Base):
    __tablename__ = "gamestates"
    __table_args__ = {'schema': 'games'}
    id = Column(BigInteger, primary_key=True)
    game_id = Column(BigInteger)
    tiles = Column(ARRAY(SmallInteger))
    playerMetadata = Column(ARRAY(SmallInteger))
    nodes = Column(ARRAY(SmallInteger))
    banks = Column(ARRAY(SmallInteger))
    edges = Column(ARRAY(SmallInteger))
    playerFullResourceCards = Column(ARRAY(SmallInteger))
    playerPerspectiveResourceCards = Column(ARRAY(SmallInteger))
    ports = Column(ARRAY(SmallInteger))
    lastRoll = Column(SmallInteger)
    currentPlayer = Column(SmallInteger)
    actionID = Column(SmallInteger)

    def __repr__(self):
        return f"<GameState(id='{self.id})>"