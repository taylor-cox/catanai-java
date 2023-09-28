import { PayloadAction, createSlice } from '@reduxjs/toolkit';
import { RootState } from '../store';

interface PageState {
  value: string,
}

const initialState = {
  value: '',
} as PageState;

export const pageSlice = createSlice({
  name: 'page',
  initialState: initialState,
  reducers: {
    changePage: (state, page: PayloadAction<string>) => {
      state.value = page.payload;
    }
  },
});

export const { changePage } = pageSlice.actions;

export const selectPage = (state: RootState) => state.page.value;

export default pageSlice.reducer;