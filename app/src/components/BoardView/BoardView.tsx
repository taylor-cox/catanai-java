/** Libraries */
import React, { useState, useEffect, useRef, FormEventHandler } from "react";
import { LeftOutlined, RightOutlined } from "@ant-design/icons";
import { Button, Input, Space } from "antd";
import { useDispatch } from "react-redux";
import { bindKey } from "@rwh/keystrokes";

/** Other views */
import Player from "./Player/Player";
import GameMetadata from "./GameMetadata/GameMetadata";

/** For getting gamestates and using them. */
import { catanapi } from "../../apis/CatanAIAPI";
import { setGameStates } from "../../features/gameStateSlice";
import { setCurrentGameState } from "../../features/currentGameState";
import { useAppSelector } from "../../hooks";
import "./BoardView.css";
import { spawn, Thread, Worker } from "threads";

const CANVAS_WIDTH = 850;
const CANVAS_HEIGHT = 850;
const BOARD_SIZE = 70;

interface Tile {
  x: number;
  y: number;
}

// TODO: Replace with getting this from the server.
let nodeEdgeMapping: number[][] = [
  [1, 2],
  [3, 4],
  [5, 6],
  [1, 7],
  [2, 3, 8],
  [4, 5, 9],
  [6, 10],
  [7, 11, 12],
  [8, 13, 14],
  [9, 15, 16],
  [10, 17, 18],
  [11, 19],
  [12, 13, 20],
  [14, 15, 21],
  [16, 17, 22],
  [18, 23],
  [19, 24, 25],
  [20, 26, 27],
  [21, 28, 29],
  [22, 30, 31],
  [23, 32, 33],
  [24, 34],
  [25, 26, 35],
  [27, 28, 36],
  [29, 30, 37],
  [31, 32, 38],
  [33, 39],
  [34, 40],
  [35, 41, 42],
  [36, 43, 44],
  [37, 45, 46],
  [38, 47, 48],
  [39, 49],
  [40, 41, 50],
  [42, 43, 51],
  [44, 45, 52],
  [46, 47, 53],
  [48, 49, 54],
  [50, 55],
  [51, 56, 57],
  [52, 58, 59],
  [53, 60, 61],
  [54, 62],
  [55, 56, 63],
  [57, 58, 64],
  [59, 60, 65],
  [61, 62, 66],
  [63, 67],
  [64, 68, 69],
  [65, 70, 71],
  [66, 72],
  [67, 68],
  [69, 70],
  [71, 72],
];

nodeEdgeMapping = nodeEdgeMapping.map((nodeEdges) =>
  nodeEdges.map((nodeEdge) => nodeEdge - 1)
);

interface Node {
  x: number;
  y: number;
}

const PLAYER_COLORS = {
  1: "#ff0000", // Red
  2: "#1e04c9", // Blue
  3: "#fff700", // Yellow
  4: "#ffffff", // White
};

const TILE_COLORS: string[] = [
  "#CEA24A", // Desert
  "#147800", // Wood
  "#9FC25C", // Sheep
  "#d19302", // Wheat
  "#c9280c", // Brick
  "#616A79", // Stone
];

const Board: React.FC = () => {
  const dispatch = useDispatch();
  const canvasRef = useRef(null);
  const currentGameState = useAppSelector((state) => state.currentGameState);
  const gameStates = useAppSelector((state) => state.gameStates.value);

  const [gameID, setGameID] = useState<number>(1);

  const gameIdChanged: FormEventHandler<HTMLInputElement> = (event) => {
    let nextGameID = parseInt(event.currentTarget.value);
    if (Number.isNaN(nextGameID) && event.currentTarget.value !== "") {
      alert("GameID must be an integer.");
      event.currentTarget.value = "";
    } else if (event.currentTarget.value !== "") {
      setGameID(parseInt(event.currentTarget.value));
    } else {
      setGameID(0);
    }
  };

  const newGameID = (_: any) => {
    catanapi.getGameByID(gameID).then((resp) => {
      const jsonData = resp.data as catanapi.IGameState[];
      dispatch(setGameStates(jsonData));
      dispatch(setCurrentGameState(0));
      drawBoard(jsonData[0], canvasRef);
    });
  };

  useEffect(() => {
    catanapi.getGameByID(gameID).then((resp) => {
      const jsonData = resp.data as catanapi.IGameState[];
      dispatch(setGameStates(jsonData));
      dispatch(setCurrentGameState(0));
      drawBoard(jsonData[0], canvasRef);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const reduceCurrentGameState = (e: any) => {
    console.log("decreasing: " + currentGameState.value);
    if (currentGameState.value === 0) return;
    dispatch(setCurrentGameState(currentGameState.value - 1));
    drawBoard(gameStates[currentGameState.value - 1], canvasRef);
  };

  const increaseCurrentGameState = (e: any) => {
    console.log("increasing: " + currentGameState.value);
    if (currentGameState.value === gameStates.length - 1) return;
    dispatch(setCurrentGameState(currentGameState.value + 1));
    drawBoard(gameStates[currentGameState.value + 1], canvasRef);
  };

  bindKey("ArrowLeft", reduceCurrentGameState);
  bindKey("ArrowRight", increaseCurrentGameState);

  return (
    <div id="board-container">
      <div id="input-and-players-col">
        <Space.Compact style={{ width: "100%" }}>
          <Input
            placeholder="Enter a game ID (1, 2, ...)"
            onInput={gameIdChanged}
          />
          <Button type="primary" onClick={newGameID}>
            Submit
          </Button>
        </Space.Compact>
        <GameMetadata />
        <Player playerID={"1"} currentTurn={true} />
        <Player playerID={"2"} currentTurn={false} />
        <Player playerID={"3"} currentTurn={false} />
        <Player playerID={"4"} currentTurn={false} />
      </div>
      <div id="board-and-scroll">
        <canvas
          id="board"
          ref={canvasRef}
          width={CANVAS_WIDTH}
          height={CANVAS_HEIGHT}
        />
        <div id="scroll-turns">
          <Button
            shape="circle"
            icon={<LeftOutlined />}
            size="large"
            onClick={reduceCurrentGameState}
          />
          <div id="button-seperator" />
          <Button
            shape="circle"
            icon={<RightOutlined />}
            size="large"
            onClick={increaseCurrentGameState}
          />
        </div>
      </div>
    </div>
  );
};

function drawBoard(
  board: catanapi.IGameState,
  canvasRef: React.MutableRefObject<any>
) {
  if (board === undefined) return;
  if (board.tiles === undefined) return;
  const ctx = canvasRef.current?.getContext("2d") as any;
  ctx.fillStyle = "#0048f0";
  ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

  function drawCenteredCatanBoard(
    x: number,
    y: number,
    size: number,
    board: catanapi.IGameState
  ): Tile[] {
    let startX = x - 2 * size * Math.sin(Math.PI / 3);
    let startY = y - 2 * size - 2 * size * Math.cos(Math.PI / 3);
    let newX = startX,
      newY = startY;
    let tiles = [];

    let numHexes = 3;
    for (let i = 0; i < 5; i++) {
      for (let j = 0; j < numHexes; j++) {
        tiles.push(drawHexagon(newX, newY, size, board.tiles![tiles.length]));
        newX += 2 * (size * Math.sin(Math.PI / 3));
      }
      numHexes = i < 2 ? numHexes + 1 : numHexes - 1;
      newY += 3 * (size * Math.cos(Math.PI / 3));
      newX = startX - (numHexes - 3) * (size * Math.sin(Math.PI / 3));
    }

    return tiles;
  }

  function drawHexagon(
    x: number,
    y: number,
    size: number,
    tile: number[]
  ): Tile {
    let angle = Math.PI / 3;
    let prevX = Math.sin(angle) * size + x;
    let prevY = Math.cos(angle) * size + y;
    angle += Math.PI / 3;

    ctx.beginPath();
    ctx.moveTo(prevX, prevY);
    for (let i = 0; i < 6; i++) {
      prevX = Math.sin(angle) * size + x;
      prevY = Math.cos(angle) * size + y;
      ctx.lineTo(prevX, prevY);
      ctx.lineWidth = 5;
      ctx.strokeStyle = "#000000";
      ctx.stroke();
      angle += Math.PI / 3;
    }
    ctx.closePath();
    ctx.fillStyle = TILE_COLORS[tile[0]];
    ctx.fill();

    return { x: x, y: y };
  }

  function getNodesOfHexagons(x: number, y: number, size: number): Node[] {
    let angle = Math.PI / 3;
    let numHexes = 3;
    let toggle = false;
    let startX = x - 2 * size * Math.sin(Math.PI / 3);
    let startY = y - 3 * size - 2 * size * Math.cos(Math.PI / 3);
    let nodes = [];

    for (let i = 0; i < 12; i++) {
      for (let j = 0; j < numHexes; j++) {
        nodes.push({ x: startX, y: startY });
        startX += Math.sin(angle) * size * 2;
      }

      if (toggle) {
        startY += size;
      } else {
        startY += Math.cos(angle) * size;
        numHexes += i < 6 ? 1 : -1;
      }
      startX = x - (numHexes - 1) * size * Math.sin(Math.PI / 3);
      toggle = !toggle;
    }

    return nodes;
  }

  function drawSettlement(node: number[], index: number, currentNode: Node) {
    let settlementColor = "";
    switch (node[0]) {
      case 0:
        return;
      case 1:
        settlementColor = PLAYER_COLORS["1"];
        break;
      case 2:
        settlementColor = PLAYER_COLORS["2"];
        break;
      case 3:
        settlementColor = PLAYER_COLORS["3"];
        break;
      case 4:
        settlementColor = PLAYER_COLORS["4"];
        break;
    }
    ctx.fillStyle = settlementColor;
    ctx.beginPath();
    ctx.moveTo(currentNode["x"], currentNode["y"]);
    ctx.arc(currentNode["x"], currentNode["y"], 10, 0, 2 * Math.PI);
    ctx.fill();
  }

  function drawRoads(
    edgesToDrawEdgesBetween: Record<number, number[]>,
    nodes: Node[],
    board: catanapi.IGameState
  ) {
    for (const edge in edgesToDrawEdgesBetween) {
      let nodeIndexesToDrawBetween = edgesToDrawEdgesBetween[edge];
      let node1 = nodes[nodeIndexesToDrawBetween[0]];
      let node2 = nodes[nodeIndexesToDrawBetween[1]];

      let roadColor = "";
      switch (board.edges[edge]) {
        case 0:
          return;
        case 1:
          roadColor = PLAYER_COLORS["1"];
          break;
        case 2:
          roadColor = PLAYER_COLORS["2"];
          break;
        case 3:
          roadColor = PLAYER_COLORS["3"];
          break;
        case 4:
          roadColor = PLAYER_COLORS["4"];
          break;
      }
      ctx.beginPath();
      ctx.moveTo(node1["x"], node1["y"]);
      ctx.lineTo(node2["x"], node2["y"]);
      ctx.strokeStyle = roadColor;
      ctx.strokeWidth = 2;
      ctx.stroke();
    }
  }

  function drawTerrainChits(tiles: Tile[], board: catanapi.IGameState) {
    tiles.forEach((tile, index) => {
      ctx.fillStyle = "#b5b5b5";
      ctx.beginPath();
      ctx.moveTo(tile["x"], tile["y"]);
      ctx.arc(tile["x"], tile["y"], 25, 0, 2 * Math.PI);
      ctx.fill();
      if (board.robberIndex === index) {
        ctx.fillStyle = "#555555";
        ctx.beginPath();
        ctx.moveTo(tile["x"], tile["y"]);
        ctx.arc(tile["x"], tile["y"], 13, 0, 2 * Math.PI);
        ctx.fill();
      }
      if (board.tiles[index][1] === 6 || board.tiles[index][1] === 8) {
        ctx.fillStyle = "#ff0000";
      } else {
        ctx.fillStyle = "#000000";
      }
      ctx.font = "20px serif";
      ctx.fillText(board.tiles[index][1], tile["x"] - 6, tile["y"]);
    });
  }

  let tiles = drawCenteredCatanBoard(
    CANVAS_WIDTH / 2,
    CANVAS_HEIGHT / 2,
    BOARD_SIZE,
    board
  );
  let nodes = getNodesOfHexagons(
    CANVAS_WIDTH / 2,
    CANVAS_HEIGHT / 2,
    BOARD_SIZE
  );
  board!.nodes!.forEach((node, index) => {
    drawSettlement(node, index, nodes[index]);
  });
  let edgesToDrawEdgesBetween: Record<number, number[]> = {};
  nodeEdgeMapping.forEach((node, nodeIndex) => {
    node.forEach((edge) => {
      if (board.edges[edge] > 0) {
        if (edgesToDrawEdgesBetween[edge] === undefined) {
          edgesToDrawEdgesBetween[edge] = [];
        }
        edgesToDrawEdgesBetween[edge].push(nodeIndex);
      }
    });
  });
  drawTerrainChits(tiles, board);
  drawRoads(edgesToDrawEdgesBetween, nodes, board);
}

export default Board;
export const playerColors: any = PLAYER_COLORS;
