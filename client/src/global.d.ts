import { string } from 'prop-types';

export interface ICourse {
  id?: string;
  name: string;
  description?: string;
  rate: number;
  category: string;
  media?: string[];
  rating?: number;
  [key: string]: any;
}

export interface ICourseUpload {
  name: string;
  description: string;
  rate: number;
  category: string;
  media: (string | File)[];
  [key: string]: any;
}

export type ICategory = string;

export interface IMedia {
  src: string;
}

export interface IUser {
  id: number | null;
  firstName: string;
  lastName?: string;
  email: string;
  avatar?: string;
  isInstructor: boolean;
  description?: string;
  role: string;
}

export interface IError {
  status: number;
  message?: string;
}

// frontend course

// instructor avatar
// instructor name
// course name
// course rating
// course rate
// course id
// instructor id

// user profile - self

// firstname
// lastname
// specialty
// email
// avatar
// description
// isInstructor

// class management

// course name
// course rate
// course category
// course description
// course id
