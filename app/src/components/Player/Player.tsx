import './Player.css';

type PlayerProps = {
  playerColor: string,
}

const Player: React.FC<PlayerProps> = (playerColor) => {
  // TODO: populate with real player information.

  return (
    <div id="player-information">
      <div id="player-color" style={{ backgroundColor: playerColor.playerColor }} />
      <div id="player-info">
        <p>Player 1</p>
        <p>Victory Points: 10</p>
        <p>Resources: 10</p>
        <p>Development Cards: 10</p>
      </div>
    </div>
  )
}

export default Player;