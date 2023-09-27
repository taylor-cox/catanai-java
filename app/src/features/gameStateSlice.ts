import { PayloadAction, createSlice } from '@reduxjs/toolkit';
import { catanapi } from '../apis/CatanAIAPI';
import { RootState } from '../store';

interface GameStateState {
  value: catanapi.IGameState[] | never[],
}

const initialState: GameStateState = {
  value: [],
};

export const gameStatesSlice = createSlice({
  name: 'gameStates',
  initialState: initialState,
  reducers: {
    setGameStates: (state, gameStates: PayloadAction<catanapi.IGameState[]>) => {
      state.value = gameStates.payload;
    }
  },
});

export const { setGameStates } = gameStatesSlice.actions;

export const selectGameStates = (state: RootState) => state.gameStates;

export default gameStatesSlice.reducer;