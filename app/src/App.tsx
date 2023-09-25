import React from 'react';
import './App.css';
import Board from './components/Board/Board';
import Navbar from './components/Navbar/Navbar';
import { LeftOutlined, RightOutlined } from '@ant-design/icons';
import { Button, Tooltip, Space } from 'antd';

function App() {
  return (
    <div className="App">
      <div id="app-container">
        <Navbar />
        <div id="board-container">
          <Board></Board>
          <div id="scroll-turns">
            <Button shape="circle" icon={<LeftOutlined />} size="large"/>
            <Button shape="circle" icon={<RightOutlined />} size="large"/>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
