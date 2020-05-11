import React, { useEffect } from 'react';
import styled from 'styled-components/macro';
import { Switch, Route } from 'react-router-dom';
import EditProfile from './pages/EditProfile';
import Header from './components/Header';
import Home from './pages/Home';
import CourseProfile from './pages/CourseProfile';
import EditCourses from './pages/EditCourses';
import Login from './pages/Login';
import AddCourse from './pages/AddCourse';
import Register from './pages/Register';
import { useDispatch } from 'react-redux';
import { getAllCategories } from './redux/AppState/appSlice';
import { authenticateToken } from './redux/UserState/userSlice';
import * as r from './Routes';
import { AppDispatch } from './redux/store';

function App() {
  const dispatch: AppDispatch = useDispatch();

  useEffect(() => {
    dispatch(getAllCategories());
    dispatch(authenticateToken());
  }, []);

  return (
    <>
      <Header />
      <Switch>
        <Route exact path={r.LOGIN_ROUTE} component={Login} />
        <Route exact path={r.REGISTER_ROUTE} component={Register} />
        <Route exact path={r.ADD_CLASS_ROUTE} component={AddCourse} />
        <Route path={r.EDIT_PROFILE_ROUTE} component={EditProfile} />
        <Route exact path={r.EDIT_CLASSES_ROUTE} component={EditCourses} />
        <Route path="/class" component={CourseProfile} />
        <Route exact path="/" component={Home} />
      </Switch>
    </>
  );
}

export default App;
