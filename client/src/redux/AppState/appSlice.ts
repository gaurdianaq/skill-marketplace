import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import Client from '../../utils/HTTPClient';
import { CATEGORIES_ROUTE } from '../../Routes';
import { AppDispatch } from '../store';
import { IError } from '../../global';

type StateShape = {
  categories: string[];
  error?: IError;
};

const initialState: StateShape = {
  categories: [],
  error: undefined,
};

export const getAllCategories = createAsyncThunk('/courses', async (param, thunkApi) => {
  const categories = await Client.request(CATEGORIES_ROUTE);
  return categories;
});

const userSlice = createSlice({
  name: 'app',
  initialState,
  reducers: {
    setError: (state, action) => {
      state.error = action.payload;
    },
  },
  extraReducers: builder => {
    builder.addCase(getAllCategories.fulfilled, (state, action) => {
      if (action.payload.error) {
        state.error = action.payload.error;
        return;
      }
      const categories = action.payload.map((category: { name: string }) => category.name);
      state.categories = categories;
    });
    builder.addCase(getAllCategories.rejected, (state, action) => {});
  },
});

export const { setError } = userSlice.actions;

export default userSlice.reducer;
