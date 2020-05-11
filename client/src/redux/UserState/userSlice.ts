import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { AppDispatch } from '../store';
import Client from '../../utils/HTTPClient';
import { COURSES_ROUTE, USERS_ROUTE, TOKEN_AUTH_ROUTE } from '../../Routes';
import { ICategory, IUser, ICourse, IError } from '../../global';

type StateShape = {
  userData: IUser;
  userCourses: ICourse[];
  error?: number;
  loading: 'idle' | 'pending';
};

const initialState: StateShape = {
  loading: 'idle',
  userData: {
    id: null,
    firstName: '',
    lastName: '',
    email: '',
    avatar: '',
    isInstructor: false,
    description: '',
    role: 'Normal',
  },
  userCourses: [],
};

export const authenticateToken = createAsyncThunk('/users/authenticate', async () => {
  const resp = await Client.request(TOKEN_AUTH_ROUTE);
  // console.log('TOKEN AUTH', resp);
  return resp;
});

export const fetchOwnCourses = createAsyncThunk('/courses/getCourses', async () => {
  const resp = await Client.request(COURSES_ROUTE);

  return resp;
});

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    setData: (state, action) => {
      state.userData = action.payload;
    },
    setCourses: (state, action) => {
      state.userCourses = action.payload;
    },
    addCourse: (state, action) => {
      state.userCourses.push(action.payload);
    },
  },
  extraReducers: builder => {
    builder.addCase(fetchOwnCourses.fulfilled, (state, action) => {
      const courses = action.payload.map((course: any) => {
        if (!course.course.media) course.course.media = [];
        return course.course;
      });
      state.userCourses = courses;
    });
    builder.addCase(authenticateToken.fulfilled, (state, action) => {
      if (action.payload.error) {
        console.log('token auth failed');
        return;
      }
      console.log('user retrieved', action.payload);
      state.userData = action.payload;
    });
    builder.addCase(authenticateToken.pending, (state, action) => {
      if (state.loading === 'idle') {
        state.loading = 'pending';
      }
    });
  },
});

export const { setData, setCourses, addCourse } = userSlice.actions;

export const postCourse = (body: ICourse) => async (dispatch: AppDispatch) => {
  const data = await Client.request(COURSES_ROUTE, 'POST', body);
  dispatch(addCourse(data));
};

export const getUser = () => async (dispatch: AppDispatch) => {
  const userData = await Client.request(USERS_ROUTE);
  dispatch(setData(userData));
};

export default userSlice.reducer;
