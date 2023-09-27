import React from 'react';
import './App.css';
import BoardView from './components/BoardView/BoardView';
import Navbar from './components/Navbar/Navbar';
import { NavigateFunction, Route, Routes, useNavigate } from 'react-router-dom';
import { useAppSelector } from './hooks';

function App() {
  const page = useAppSelector((state) => state.page);

  // Navigation for Navbar
  const navigate: NavigateFunction = useNavigate();
  if (window.location.href.split('/').pop() !== page.value) {
    console.log(window.location.href.split('/'))
    navigate(page.value);
  }

  return (
    <div className="App">
      <div id="app-container">
        <Navbar />
        <div id="changing-container">
          <Routes>
            <Route path='/' element={<BoardView />} />
            <Route path='/statistics' element={<div>Statistics</div>} />
            <Route path='/database' element={<div>Database</div>} />
            <Route path='/settings' element={<div>Settings</div>} />
          </Routes>
        </div>
      </div>
    </div>
  );
}

export default App;
