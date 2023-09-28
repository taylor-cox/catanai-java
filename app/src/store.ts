import { configureStore } from '@reduxjs/toolkit'
import pageReducer from './features/pageSlice';
import gameStatesReducer from './features/gameStateSlice';
import currentGameStateReducer from './features/currentGameState';

const store = configureStore({
  reducer: {
    page: pageReducer,
    gameStates: gameStatesReducer,
    currentGameState: currentGameStateReducer,
  },
})


export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export default store;