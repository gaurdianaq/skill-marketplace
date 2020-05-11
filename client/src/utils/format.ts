import { IUser } from '../global';

export const userCourses = (dbObject: any) => {
  const {
    course_category,
    course_id,
    course_name,
    course_rate,
    instructor_id,
    instructor_name,
  } = dbObject;

  return {
    id: course_id,
    name: course_name,
    category: course_category,
    rate: course_rate,
    instructorId: instructor_id,
    instructorName: instructor_name,
  };
};

export const userObject = (dbObject: Record<string, any>) => {
  // const { email, first_name, last_name, is_instructor, role } = dbObject;
  // return {
  //   email,
  //   firstName: first_name,
  //   lastName: last_name,
  //   isInstructor: is_instructor,
  //   role,
  // };
};

export default {
  userCourses,
  userObject,
};
