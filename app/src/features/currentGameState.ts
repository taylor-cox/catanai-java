import { PayloadAction, createSlice } from '@reduxjs/toolkit';

export interface CurrentGameState {
  value: number,
}

const initialState = {
  value: 0,
} as CurrentGameState;

export const currentGameStateSlice = createSlice({
  name: 'currentGameState',
  initialState: initialState,
  reducers: {
    setCurrentGameState: (state, page: PayloadAction<number>) => {
      state.value = page.payload;
    }
  },
});

export const { setCurrentGameState } = currentGameStateSlice.actions;

export const selectCurrentGameState = (state: any) => state.currentGameState.value;

export default currentGameStateSlice.reducer;