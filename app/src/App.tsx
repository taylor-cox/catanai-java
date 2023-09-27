import React from 'react';
import './App.css';
import Board from './components/Board/Board';
import Navbar from './components/Navbar/Navbar';
import { NavigateFunction, Route, Routes, useNavigate } from 'react-router-dom';
import { useAppSelector } from './hooks';

function App() {
  const page = useAppSelector((state) => state.page);

  // Navigation for Navbar
  const navigate: NavigateFunction = useNavigate();

  navigate(page.value);
  return (
    <div className="App">
      <div id="app-container">
        <Navbar />
        <div id="changing-container">
          <Routes>
            <Route path='/' element={<Board />} />
          </Routes>
        </div>
      </div>
    </div>
  );
}

export default App;
