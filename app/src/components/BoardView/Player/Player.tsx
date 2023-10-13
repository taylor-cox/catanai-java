import { UserOutlined } from "@ant-design/icons";
import "./Player.css";
import BrickPNG from "../../../icons/brick.png";
import SheepPNG from "../../../icons/sheep.png";
import OrePNG from "../../../icons/rock.png";
import WheatPNG from "../../../icons/wheat.png";
import WoodPNG from "../../../icons/wood-plank.png";
import RoadPNG from "../../../icons/road.png";
import CityPNG from "../../../icons/city.png";
import HomePNG from "../../../icons/home.png";
import StarPNG from "../../../icons/star.png";
import LastActionPNG from "../../../icons/last-action.png";
import KnightPNG from "../../../icons/knight.png";
import LongestRoadPNG from "../../../icons/longest-road.png";
import ArmyPNG from "../../../icons/army.png";
import CardPNG from "../../../icons/card.png";

import { playerColors } from "../BoardView";
import { useAppSelector } from "../../../hooks";

/*
TODO: Add attribution to here:
<a href="https://www.flaticon.com/free-icons/brick" title="brick icons">Brick icons created by Smashicons - Flaticon</a>
*/

type PlayerProps = {
  playerID: string;
  currentTurn: boolean;
};

const starIconStyle = {
  color: "#FFF700",
  width: "1.4rem",
  paddingLeft: "0.4rem",
  paddingRight: "0.4rem",
};

const pngIconStyle = {
  width: "1rem",
  paddingLeft: "0.4rem",
  paddingRight: "0.4rem",
};

const Player: React.FC<PlayerProps> = (playerProps) => {
  /**
   * This component displays the current player's information.
   *
   * This includes their victory points, buildings left, resources,
   * development cards, and other metadata such as longest road and
   * largest army.
   */
  const currentGameState = useAppSelector(
    (state) => state.currentGameState.value
  );
  const gameStates = useAppSelector((state) => state.gameStates.value);

  let isLoading = true;

  if (gameStates.length !== 0) {
    isLoading = false;
  }

  // Setting background color for player icons
  let playerIconBackgroundColor = "#cdcdcd";
  const playerID = playerProps.playerID;
  if (
    playerColors[playerID] === "#ffffff" ||
    playerColors[playerID] === "#fff700"
  ) {
    playerIconBackgroundColor = "#cdcdcd";
  }

  // Setting style for player's background (highlight if current turn, etc.)
  let playerStyle = {
    backgroundColor: "",
    borderRadius: "0.5rem",
  };
  if (gameStates[currentGameState]?.currentPlayer === parseInt(playerID)) {
    playerStyle.backgroundColor = "#dee3fa";
  }

  if (isLoading) {
    return <div></div>;
  }

  return (
    <div id="player-information" style={playerStyle}>
      <div id="player-icon">
        <UserOutlined
          style={{
            color: playerColors[playerID],
            backgroundColor: playerIconBackgroundColor,
            padding: "0.2rem",
            borderRadius: "0.25rem",
            marginRight: "0.4rem",
            minWidth: "1rem",
          }}
        />
        <p>{playerID}</p>
      </div>
      <div id="player-current-state">
        {/* ----------- Player Victory Points + Buildings Left ------------- */}
        <div id="player-victory-points-buildings">
          <p style={{ textDecoration: "underline" }}>Points / Buildings</p>
          <div id="player-vp" title="Victory Points">
            <img
              src={StarPNG}
              style={starIconStyle}
              alt="player victory points"
              id="star-png"
            />
            <p>
              {
                gameStates[currentGameState].playerMetadata[
                  Number.parseInt(playerID) - 1
                ][0]
              }
            </p>
          </div>
          <div id="player-settlements" title="Settlements">
            <img src={HomePNG} style={pngIconStyle} alt="player settlements" />
            <p>
              {
                gameStates[currentGameState].playerMetadata[
                  Number.parseInt(playerID) - 1
                ][3]
              }
            </p>
          </div>
          <div id="player-roads" title="Roads">
            <img src={RoadPNG} style={pngIconStyle} alt="player roads" />
            <p>
              {
                gameStates[currentGameState].playerMetadata[
                  Number.parseInt(playerID) - 1
                ][5]
              }
            </p>
          </div>
          <div id="player-cities" title="Cities">
            <img src={CityPNG} style={pngIconStyle} alt="player cities" />
            <p>
              {
                gameStates[currentGameState].playerMetadata[
                  Number.parseInt(playerID) - 1
                ][4]
              }
            </p>
          </div>
        </div>
        {/* ------------------------ Player Resources ---------------------- */}
        <div id="player-resources">
          <p style={{ textDecoration: "underline" }}>Resources</p>
          <div id="player-brick" title="Brick">
            <img
              src={BrickPNG}
              style={pngIconStyle}
              alt="player brick amount"
              id="brick-png"
            />
            <p>
              {
                gameStates[currentGameState].playerFullResourceCards[
                  Number.parseInt(playerID) - 1
                ][4]
              }
            </p>
          </div>
          <div id="player-wood" title="Wood">
            <img
              src={WoodPNG}
              style={pngIconStyle}
              alt="player wood amount"
              id="wood-png"
            />
            <p>
              {
                gameStates[currentGameState].playerFullResourceCards[
                  Number.parseInt(playerID) - 1
                ][2]
              }
            </p>
          </div>
          <div id="player-wheat" title="Wheat">
            <img
              src={WheatPNG}
              style={pngIconStyle}
              alt="player wheat amount"
              id="wheat-png"
            />
            <p>
              {
                gameStates[currentGameState].playerFullResourceCards[
                  Number.parseInt(playerID) - 1
                ][1]
              }
            </p>
          </div>
          <div id="player-sheep" title="Sheep">
            <img
              src={SheepPNG}
              style={pngIconStyle}
              alt="player sheep amount"
              id="sheep-png"
            />
            <p>
              {
                gameStates[currentGameState].playerFullResourceCards[
                  Number.parseInt(playerID) - 1
                ][0]
              }
            </p>
          </div>
          <div id="player-ore" title="Ore">
            <img
              src={OrePNG}
              style={pngIconStyle}
              alt="player ore amount"
              id="ore-png"
            />
            <p>
              {
                gameStates[currentGameState].playerFullResourceCards[
                  Number.parseInt(playerID) - 1
                ][3]
              }
            </p>
          </div>
        </div>
        {/* ----------------------- Player Dev Cards ----------------------- */}
        <div id="player-resources">
          <p style={{ textDecoration: "underline" }}>Dev. Cards</p>
          <div id="player-knight-dev" title="Knight">
            <img
              src={KnightPNG}
              style={pngIconStyle}
              alt="player knight dev cards in hand"
            />
            <p>
              {
                gameStates[currentGameState].playerDevelopmentCards[
                  Number.parseInt(playerID) - 1
                ][0]
              }
            </p>
          </div>
          <div id="player-vp-dev" title="VP">
            <img
              src={StarPNG}
              style={pngIconStyle}
              alt="player victory point dev cards in hand"
            />
            <p>
              {
                gameStates[currentGameState].playerDevelopmentCards[
                  Number.parseInt(playerID) - 1
                ][4]
              }
            </p>
          </div>
          <div id="player-road-building-dev" title="Road Building">
            <img
              src={RoadPNG}
              style={pngIconStyle}
              alt="player road building dev cards in hand"
            />
            <p>
              {
                gameStates[currentGameState].playerDevelopmentCards[
                  Number.parseInt(playerID) - 1
                ][1]
              }
            </p>
          </div>
          <div id="player-yop-dev" title="Year of Plenty">
            <img
              src={CardPNG}
              style={pngIconStyle}
              alt="player year of plenty dev cards in hand"
            />
            <p>
              {
                gameStates[currentGameState].playerDevelopmentCards[
                  Number.parseInt(playerID) - 1
                ][2]
              }
            </p>
          </div>
          <div id="player-monopoly-dev" title="Monopoly">
            <img
              src={CardPNG}
              style={pngIconStyle}
              alt="player monopoly dev cards in hand"
            />
            <p>
              {
                gameStates[currentGameState].playerDevelopmentCards[
                  Number.parseInt(playerID) - 1
                ][3]
              }
            </p>
          </div>
        </div>
        {/* --------------------- Player Other Metadata -------------------- */}
        <div id="player-other-metadata">
          <p style={{ textDecoration: "underline" }}>Metadata</p>
          <div id="player-last-action" title="Last action">
            <img
              src={LastActionPNG}
              style={pngIconStyle}
              alt="player last action"
            />
            <p>N/A</p>
          </div>
          <div id="player-robbers-played" title="Knights played">
            <img
              src={KnightPNG}
              style={pngIconStyle}
              alt="player number of robbers played"
            />
            <p>
              {
                gameStates[currentGameState].playerMetadata[
                  Number.parseInt(playerID) - 1
                ][6]
              }
            </p>
          </div>
          <div id="player-longest-road" title="Longest road">
            <img
              src={LongestRoadPNG}
              style={pngIconStyle}
              alt="player longest road"
            />
            <p>
              {gameStates[currentGameState].playerDevelopmentCards[
                Number.parseInt(playerID) - 1
              ][2] === 1
                ? "Yes"
                : "No"}
            </p>
          </div>
          <div id="player-largest-army" title="Largest army">
            <img
              src={ArmyPNG}
              style={pngIconStyle}
              alt="player number of robbers played"
            />
            <p>
              {gameStates[currentGameState].playerDevelopmentCards[
                Number.parseInt(playerID) - 1
              ][1] === 1
                ? "Yes"
                : "No"}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Player;
