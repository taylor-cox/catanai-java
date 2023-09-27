import axios, { AxiosResponse } from 'axios';

export namespace catanapi {
  const HOST_IP = "192.168.1.108";
  const HOST_PORT = "8080";
  const API_VERSION = "v1";
  const API_NETWORK_LOCATION = `http://${HOST_IP}:${HOST_PORT}/api/${API_VERSION}`;

  export interface IBoard {
    tiles?: number[][],
    banks?: number[][],
    playerPerspectiveResourceCards?: number[][],
    playerFullResourceCards?: number[][],
    edges?: number[][],
    nodes?: number[][],
    ports?: number[][],
    playerMetadata?: number[][],
    lastRoll?: number[][],
    currentPlayer?: number[][],
    actionID?: number[][],
    finished?: number[][],
    actionState?: number[][],
  };

  // TODO: Remove testing function
  export function getRandomBoard(): Promise<AxiosResponse<IBoard, any>> {
    return axios.get<IBoard>(`${API_NETWORK_LOCATION}/randomGame`, { method: 'GET', headers: {"Accept": 'application/json'}});
  }

  export function getNodeEdgeMappings(): Promise<AxiosResponse> {
    return axios.get(`${API_NETWORK_LOCATION}/nodeEdgeMappings`);
  }

  export function getGameByID(gameID: string): Promise<AxiosResponse> {
    return axios.get(`${API_NETWORK_LOCATION}/game?gameId=${gameID}`);
  }
};