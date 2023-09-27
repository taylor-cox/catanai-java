import { UserOutlined } from '@ant-design/icons';
import './Player.css';
import BrickPNG from '../../../icons/brick.png';
import SheepPNG from '../../../icons/sheep.png';
import OrePNG from '../../../icons/rock.png';
import WheatPNG from '../../../icons/wheat.png';
import WoodPNG from '../../../icons/wood-plank.png';
import RoadPNG from '../../../icons/road.png';
import CityPNG from '../../../icons/city.png';
import HomePNG from '../../../icons/home.png';
import StarPNG from '../../../icons/star.png';
import LastActionPNG from '../../../icons/last-action.png';
import KnightPNG from '../../../icons/knight.png';
import LongestRoadPNG from '../../../icons/longest-road.png';
import ArmyPNG from '../../../icons/army.png';

import {playerColors} from '../BoardView';


/*
TODO: Add attribution to here:
<a href="https://www.flaticon.com/free-icons/brick" title="brick icons">Brick icons created by Smashicons - Flaticon</a>
*/

type PlayerProps = {
  playerID: string,
  currentTurn: boolean
}

const starIconStyle = {
  color: '#FFF700',
  width: '1.4rem',
  paddingLeft: '0.4rem',
  paddingRight: '0.4rem'
}

const pngIconStyle = {
  width: '1rem',
  paddingLeft: '0.4rem',
  paddingRight: '0.4rem'
}

const Player: React.FC<PlayerProps> = (playerProps) => {
  // TODO: populate with real player information from state.

  // Setting background color for player icons
  let playerIconBackgroundColor = '#d0d0d0';
  const playerID = playerProps.playerID;
  if (playerColors[playerID] === '#ffffff' || playerColors[playerID] === '#fff700') {
    playerIconBackgroundColor = '#696969';
  }

  // Setting style for player's background (highlight if current turn, etc.)
  let playerStyle = {
    backgroundColor: '',
    borderRadius: '0.5rem',
  };
  if (playerProps.currentTurn) {
    playerStyle.backgroundColor = '#dee3fa';
  }
  return (
    <div id="player-information" style={playerStyle}>
      <div id="player-icon">
        <UserOutlined style={{
          color: playerColors[playerID],
          backgroundColor: playerIconBackgroundColor,
          padding: '0.2rem', borderRadius: "0.25rem",
          marginRight: '0.4rem',
          minWidth: '1rem'
        }}/>
        <p>{playerID}</p>
      </div>
      <div id="player-current-state">
        {/* ----------- Player Victory Points + Buildings Left ------------- */}
        <div id="player-victory-points-buildings">
          <p style={{textDecoration: 'underline'}}>Points / Buildings</p>
          <div id="player-vp" title="Victory Points">
            <img src={StarPNG} style={starIconStyle} alt="player victory points" id="star-png"/>
            <p>10</p>
          </div>
          <div id="player-settlements" title="Settlements">
            <img src={HomePNG} style={pngIconStyle} alt="player settlements"/>
            <p>5</p>
          </div>
          <div id="player-roads" title="Roads">
            <img src={RoadPNG} style={pngIconStyle} alt="player roads"/>
            <p>15</p>
          </div>
          <div id="player-cities" title="Cities">
            <img src={CityPNG} style={pngIconStyle} alt="player cities"/>
            <p>5</p>
          </div>
        </div>
        {/* ------------------------ Player Resources ---------------------- */}
        <div id="player-resources">
          <p style={{textDecoration: 'underline'}}>Resources</p>
          <div id="player-brick" title="Brick">
            <img src={BrickPNG} style={pngIconStyle} alt="player brick amount" id="brick-png"/>
            <p>1</p>
          </div>
          <div id="player-wood" title="Wood">
            <img src={WoodPNG} style={pngIconStyle} alt="player wood amount" id="wood-png"/>
            <p>1</p>
          </div>
          <div id="player-wheat" title="Wheat">
            <img src={WheatPNG} style={pngIconStyle} alt="player wheat amount" id="wheat-png"/>
            <p>1</p>
          </div>
          <div id="player-sheep" title="Sheep">
            <img src={SheepPNG} style={pngIconStyle} alt="player sheep amount" id="sheep-png"/>
            <p>1</p>
          </div>
          <div id="player-ore" title="Ore">
            <img src={OrePNG} style={pngIconStyle} alt="player ore amount" id="ore-png"/>
            <p>1</p>
          </div>
        </div>
        {/* ----------------------- Player Dev Cards ----------------------- */}
        <div id="player-resources">
          <p style={{textDecoration: 'underline'}}>Dev. Cards</p>
          <div id="player-brick" title="Brick">
            <img src={BrickPNG} style={pngIconStyle} alt="player brick amount" id="brick-png"/>
            <p>1</p>
          </div>
          <div id="player-wood" title="Wood">
            <img src={WoodPNG} style={pngIconStyle} alt="player wood amount" id="wood-png"/>
            <p>1</p>
          </div>
          <div id="player-wheat" title="Wheat">
            <img src={WheatPNG} style={pngIconStyle} alt="player wheat amount" id="wheat-png"/>
            <p>1</p>
          </div>
          <div id="player-sheep" title="Sheep">
            <img src={SheepPNG} style={pngIconStyle} alt="player sheep amount" id="sheep-png"/>
            <p>1</p>
          </div>
          <div id="player-ore" title="Ore">
            <img src={OrePNG} style={pngIconStyle} alt="player ore amount" id="ore-png"/>
            <p>1</p>
          </div>
        </div>

        {/* --------------------- Player Other Metadata -------------------- */}
        <div id="player-other-metadata">
          <p style={{textDecoration: 'underline'}}>Metadata</p>
          <div id="player-last-action" title="Last action">
            <img src={LastActionPNG} style={pngIconStyle} alt="player last action" />
            <p>END_TURN</p>
          </div>
          <div id="player-robbers-played" title='Knights played'>
            <img src={KnightPNG} style={pngIconStyle} alt="player number of robbers played" />
            <p>0</p>
          </div>
          <div id="player-longest-road" title='Longest road'>
            <img src={LongestRoadPNG} style={pngIconStyle} alt="player longest road" />
            <p>No</p>
          </div>
          <div id="player-largest-army" title='Largest army'>
            <img src={ArmyPNG} style={pngIconStyle} alt="player number of robbers played" />
            <p>No</p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Player;