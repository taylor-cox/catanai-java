import React from 'react';
import './App.css';
import Board from './components/Board/Board';
import Navbar, { NavPairArray } from './components/Navbar/Navbar';

function App() {
  let navs: NavPairArray = [];
  navs.push({name: 'Home', url: '/home'});
  navs.push({name: 'Database', url: '/home'});
  navs.push({name: 'Terms and Conditions', url: '/home'});
  return (
    <div className="App">
      <Navbar navOptions={navs}></Navbar>
      <Board tiles={[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]}></Board>
    </div>
  );
}

export default App;
