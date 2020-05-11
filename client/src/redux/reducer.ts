import { combineReducers } from '@reduxjs/toolkit';
import app from './AppState/appSlice';
import user from './UserState/userSlice';

const rootReducer = combineReducers({
  app,
  user,
});

export type RootState = ReturnType<typeof rootReducer>;
export default rootReducer;
