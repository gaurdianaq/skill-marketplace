// Front End Routes
export const LOGIN_ROUTE = '/login';
export const TEMP_COURSE_ROUTE = '/addcourse';
export const REGISTER_ROUTE = '/register';
export const PROFILE_ROUTE = '/profile';
export const HOME_ROUTE = '/home';
export const CLASSES_ROUTE = '/classes';
export const EDIT_CLASSES_ROUTE = PROFILE_ROUTE + CLASSES_ROUTE;
export const EDIT_PROFILE_ROUTE = PROFILE_ROUTE + HOME_ROUTE;
export const ADD_CLASS_ROUTE = CLASSES_ROUTE + '/new';

// Back End Routes
export const TOKEN_AUTH_ROUTE = '/session/authenticate';
export const BACKEND_LOGIN_ROUTE = '/session/login';
export const BACKEND_LOGOUT_ROUTE = '/session/logout';
export const AUTHENTICATION_ROUTE = '/session/authentication';
export const ADD_COURSE_ROUTE = '/courses';
export const INSTRUCTOR_COURSES_ROUTE = '/courses/instructor'
export const COURSES_ROUTE = '/courses';
// ! should get avg ratings
// get - gets all courses
// get /<courseId> get course info
// post - new course
export const USERS_ROUTE = '/users';
// get - current user?
// post - new user
export const CATEGORIES_ROUTE = '/categories';
export const CONTACT_METHOD_ROUTE = '/contact_methods';
export const CONTACT_INFO_ROUTE = '/contact_info';
export const USER_COURSES_ROUTE =  '/user_courses';
export const RATINGS_ROUTE = '/ratings';