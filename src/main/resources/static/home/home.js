const boardCanvas = document.getElementById('board');
const boardCtx = boardCanvas.getContext('2d');

let tiles = [];

function main() {
    const MIDDLE_X = 300;
    const MIDDLE_Y = 300;
    const SIZE = 50;
    tiles = drawCenteredCatanBoard(MIDDLE_X, MIDDLE_Y, SIZE);
    tiles.forEach((tile) => {
        // const img = new Image();
        // img.onload = () => {
        //     boardCtx.drawImage(img, tile['x'] - (0.5 * SIZE), tile['y'] - (0.5 * SIZE), SIZE, SIZE);
        // };
        // img.src = '/static/images/grain.png'
    });
    let nodePoints = getNodesOfHexagons(MIDDLE_X, MIDDLE_Y, SIZE);
    // nodes.forEach((node) => {
    //     boardCtx.moveTo(node['x'], node['y']);
    //     boardCtx.arc(node['x'], node['y'], 10, 0, 2 * Math.PI);
    //     boardCtx.stroke();
    // });
    console.log(nodePoints)
    boardCtx.moveTo(nodePoints[10]['x'], nodePoints[10]['y']);
    boardCtx.beginPath();
    boardCtx.arc(nodePoints[10]['x'], nodePoints[10]['y'], 10, 0, 2 * Math.PI);
    boardCtx.stroke();
}

function drawCenteredCatanBoard(x, y, size) {
    let startX = x - (2 * size * Math.sin(Math.PI / 3));
    let startY = y - (2 * size) - (2 * size * Math.cos(Math.PI / 3));
    let newX = startX, newY = startY;
    let tiles = [];

    let numHexes = 3;
    for(let i = 0; i < 5; i ++) {
        for(let j = 0; j < numHexes; j++) {
            tiles.push(drawHexagon(newX, newY, size));
            newX += 2 * (size * Math.sin(Math.PI / 3));
        }
        numHexes = i < 2 ?  numHexes + 1 : numHexes - 1;
        newY += 3 * (size * Math.cos(Math.PI / 3));
        newX = startX - (numHexes - 3) * (size * Math.sin(Math.PI / 3));
    }

    return tiles;
}

function drawHexagon(x, y, size) {
    let angle = Math.PI / 3;
    let prevX = (Math.sin(angle) * size) + x;
    let prevY = (Math.cos(angle) * size) + y;
    angle += Math.PI / 3;
    for(let i = 0; i < 6; i++) {
        boardCtx.moveTo(prevX, prevY);
        prevX = (Math.sin(angle) * size) + x;
        prevY = (Math.cos(angle) * size) + y;
        boardCtx.lineTo(prevX, prevY);
        boardCtx.stroke();
        angle += Math.PI / 3;
        
        // prevX = (Math.sin(angle) * size) + x;
        // prevY = (Math.cos(angle) * size) + y;
        // boardCtx.arc(x, y, 10, 0, 2 * Math.PI);
        // boardCtx.stroke();
        // angle += Math.PI / 3;
    }
    return {'x': x, 'y': y}
}

function getNodesOfHexagons(x, y, size) {
    let angle = Math.PI / 3;
    let numHexes = 3;
    let toggle = false;
    let startX = x - (2 * size * Math.sin(Math.PI / 3));
    let startY = y - (3 * size) - (2 * size * Math.cos(Math.PI / 3));
    let nodes = [];

    for(let i = 0; i < 12; i++) {
        for(let j = 0; j < numHexes; j++) {
            // boardCtx.moveTo(startX, startY);
            // boardCtx.arc(startX, startY, 10, 0, 2 * Math.PI);
            // boardCtx.stroke();
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

main();