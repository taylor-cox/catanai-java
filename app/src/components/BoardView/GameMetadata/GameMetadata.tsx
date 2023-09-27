import React from 'react';
import './GameMetadata.css';

const GameMetadata: React.FC = () => {
  let lastDiceRoll = 8;
  return (
    <div id="game-metadata-container">
      <div id="title">
        <h5>Game Data:</h5>
      </div>
      <div className="row">
        <p>Last Dice Roll: {lastDiceRoll}</p>
        <p>Action State: 1</p>
        <p>Current Player: 1</p>
      </div>
      <div className="row">
        <p>Finished: False</p>
        <p>Total Actions: 109</p>
        <p>Total Turns: 35</p>
      </div>
    </div>
  );
}

export default GameMetadata;