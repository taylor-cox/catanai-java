import React from "react";
import "./GameMetadata.css";
import { useAppSelector } from "../../../hooks";

const GameMetadata: React.FC = () => {
  const currentGameState = useAppSelector(
    (state) => state.currentGameState.value
  );
  const gameStates = useAppSelector((state) => state.gameStates.value);
  let isLoading: boolean = gameStates.length === 0;

  const getActionString = (action: number) => {
    switch (action) {
      case 0:
        return "New Game";
      case 1:
        return "Play Road";
      case 2:
        return "Play Settlement";
      case 3:
        return "Play City";
      case 4:
        return "Play Knight";
      case 5:
        return "Play Road Building";
      case 6:
        return "Play Year of Plenty";
      case 7:
        return "Play Monopoly";
      case 8:
        return "Draw Development Card";
      case 9:
        return "Offer Trade";
      case 10:
        return "Accept Trade";
      case 11:
        return "Decline Trade";
      case 12:
        return "Move Robber";
      case 13:
        return "Discard";
      case 14:
        return "End Turn";
      case 15:
        return "Roll Dice";
    }
  };

  if (isLoading) {
    return <div></div>;
  }

  return (
    <div id="game-metadata-container">
      <div id="title">
        <h5>Game Data:</h5>
      </div>
      <div className="row">
        <p>Last Dice Roll: {gameStates[currentGameState].lastRoll}</p>
        <p>
          Action Performed:{" "}
          {getActionString(gameStates[currentGameState].actionID)}
        </p>
        <p>Current Player: {gameStates[currentGameState].currentPlayer}</p>
      </div>
      <div className="row">
        <p>Total Actions: {gameStates.length}</p>
        <p>Current Action: {currentGameState + 1}</p>
      </div>
    </div>
  );
};

export default GameMetadata;
