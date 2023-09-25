import React, {useState, useEffect, useRef} from 'react';
import './Board.css';
import { catanapi } from '../../apis/CatanAIAPI';

interface BoardProps {
  tiles: number[],
}

interface Tile {
  x: number,
  y: number
};

interface Node {
  x: number,
  y: number
};

interface Edge {
  index: number
};

const CANVAS_WIDTH = 850;
const CANVAS_HEIGHT = 850;
const BOARD_SIZE = 70;

const PLAYER_COLORS = {
  P1: "#ff0000",
  P2: "#1e04c9",
  P3: "#fff700",
  P4: "#ffffff"
};

const TILE_COLORS: string[] = [
  "#CEA24A",
  "#9FC25C",
  "#d19302",
  "#147800",
  "#616A79",
  "#c9280c"
]

function Board() {
  const [board, setBoard] = useState<catanapi.IBoard>();
  const canvasRef = useRef(null);

  useEffect(() => {
    catanapi.getRandomBoard()
    .then((resp) => {
      setBoard(resp.data);
    });

  }, []);

  useEffect(() => {
    let nodes = drawBoard(board, canvasRef);
  }, [board]);

  return (
    <div id="board-container">
      <canvas id="board" ref={canvasRef} width={CANVAS_WIDTH} height={CANVAS_HEIGHT} />
      {/* <p>{JSON.stringify(board)}</p> */}
    </div>
  );
}

function drawBoard(board: catanapi.IBoard | undefined, canvasRef: React.MutableRefObject<any>) {
  if (board === undefined) return;
  if (board.tiles === undefined) return;
  const ctx = canvasRef.current?.getContext('2d') as any;
  ctx.fillStyle = "#0048f0";
  ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

  function drawCenteredCatanBoard(x: number, y: number, size: number, board: catanapi.IBoard): Tile[] {
    let startX = x - (2 * size * Math.sin(Math.PI / 3));
    let startY = y - (2 * size) - (2 * size * Math.cos(Math.PI / 3));
    let newX = startX, newY = startY;
    let tiles = [];

    let numHexes = 3;
    for(let i = 0; i < 5; i ++) {
      for(let j = 0; j < numHexes; j++) {
          console.log(tiles.length, tiles.length + 1);
          tiles.push(drawHexagon(newX, newY, size, board.tiles![tiles.length]));
          newX += 2 * (size * Math.sin(Math.PI / 3));
      }
      numHexes = i < 2 ?  numHexes + 1 : numHexes - 1;
      newY += 3 * (size * Math.cos(Math.PI / 3));
      newX = startX - (numHexes - 3) * (size * Math.sin(Math.PI / 3));
    }

    return tiles;
  }

  function drawHexagon(x: number, y: number, size: number, tile: number[]): Tile {
    let angle = Math.PI / 3;
    let prevX = (Math.sin(angle) * size) + x;
    let prevY = (Math.cos(angle) * size) + y;
    angle += Math.PI / 3;
    
    console.log(tile[0]);
    ctx.beginPath();
    ctx.moveTo(prevX, prevY);
    for(let i = 0; i < 6; i++) {
        prevX = (Math.sin(angle) * size) + x;
        prevY = (Math.cos(angle) * size) + y;
        ctx.lineTo(prevX, prevY);
        ctx.lineWidth = 5;
        ctx.fillstyle = '#000000';
        ctx.stroke();
        angle += Math.PI / 3;
    }
    ctx.closePath();
    ctx.fillStyle = TILE_COLORS[tile[0]];
    ctx.fill();

    return {'x': x, 'y': y}
  }

  function getNodesOfHexagons(x: number, y: number, size: number): Node[] {
      let angle = Math.PI / 3;
      let numHexes = 3;
      let toggle = false;
      let startX = x - (2 * size * Math.sin(Math.PI / 3));
      let startY = y - (3 * size) - (2 * size * Math.cos(Math.PI / 3));
      let nodes = [];
  
      for(let i = 0; i < 12; i++) {
          for(let j = 0; j < numHexes; j++) {
              nodes.push({'x': startX, 'y': startY});
              startX += (Math.sin(angle) * size) * 2;
          }
  
          if(toggle) {
              startY += size;
          } else {
              startY += (Math.cos(angle) * size);
              numHexes += i < 6 ? 1 : -1;
          }
          startX = x - ((numHexes - 1) * size * Math.sin(Math.PI / 3));
          toggle = !toggle;
      }
  
      return nodes;
  }

  function drawSettlement(node: number[], index: number, currentNode: Node) {
    let settlementColor = '';
    switch(node[0]) {
      case 0:
        return;
      case 1:
        settlementColor = PLAYER_COLORS['P1'];
        break;
      case 2:
        settlementColor = PLAYER_COLORS['P2'];
        break;
      case 3:
        settlementColor = PLAYER_COLORS['P3'];
        break;
      case 4:
        settlementColor = PLAYER_COLORS['P4'];
        break;
    }
    ctx.fillStyle = settlementColor;
    ctx.beginPath();
    ctx.moveTo(currentNode['x'], currentNode['y']);
    ctx.arc(currentNode['x'], currentNode['y'], 10, 0, 2 * Math.PI);
    ctx.fill();
  }

  // function drawRoad(road: number[], index: number, currentEdge: Edge) {
  //   let settlementColor = '';
  //   switch(node[0]) {
  //     case 0:
  //       return;
  //     case 1:
  //       settlementColor = PLAYER_COLORS['P1'];
  //       break;
  //     case 2:
  //       settlementColor = PLAYER_COLORS['P2'];
  //       break;
  //     case 3:
  //       settlementColor = PLAYER_COLORS['P3'];
  //       break;
  //     case 4:
  //       settlementColor = PLAYER_COLORS['P4'];
  //       break;
  //   }
  //   ctx.fillStyle = settlementColor;
  //   ctx.beginPath();
  //   ctx.moveTo(currentNode['x'], currentNode['y']);
  //   ctx.arc(currentNode['x'], currentNode['y'], 10, 0, 2 * Math.PI);
  //   ctx.fill();
  // }

  drawCenteredCatanBoard(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, BOARD_SIZE, board);
  let nodes = getNodesOfHexagons(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, BOARD_SIZE);
  board!.nodes!.forEach((node, index) => {
    drawSettlement(node, index, nodes[index]);
  });
}

export default Board;