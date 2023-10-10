// import React, { useState, useEffect, useRef, FormEventHandler } from "react";
import { Table } from "antd";
import "./DatabaseView.css";
import { ColumnsType } from "antd/es/table";
import React from "react";
// import { catanapi } from "../../apis/CatanAIAPI";
// import { useDispatch } from "react-redux";
// import { setGameStates } from "../../features/gameStateSlice";
// import { setCurrentGameState } from "../../features/currentGameState";
// import { useAppSelector } from "../../hooks";

interface DataType {
  key: React.ReactNode;
  gameID: number;
  children?: DataType[];
}

const columns: ColumnsType<DataType> = [
  {
    title: "Game ID",
    dataIndex: "gameID",
    key: "gameID",
  },
];

const DatabaseView: React.FC = () => {
  const expandedRowRender = () => {
    // Need to request the game states from the server.
    const columns = [{ title: "" }];
    return <p>Test</p>;
  };
  return (
    <div id="database-view">
      <Table columns={columns} expandable={{ expandedRowRender }} />
    </div>
  );
};

export default DatabaseView;
