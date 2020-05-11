import React, { useEffect } from 'react';
import styled from 'styled-components/macro';
import Layout from '../components/Layout/ProfilePageLayout';
import Row from '../components/EditCourse/CourseRow';
import { Link } from 'react-router-dom';
import Button from '../components/Button';
import { ADD_CLASS_ROUTE } from '../Routes';
import { useSelector } from 'react-redux';
import { RootState } from '../redux/reducer';
import { useDispatch } from 'react-redux';
import { AppDispatch } from '../redux/store';
import { fetchOwnCourses } from '../redux/UserState/userSlice';

type Flex = {
  flex?: number;
};

const Table = styled.div`
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  background-color: hsla(181, 25%, 25%, 0.25);
  box-shadow: 0px 10px 23px 0px rgba(0, 0, 0, 0.2);
  overflow: hidden;
`;

const Column = styled.div<Flex>`
  flex: ${p => p.flex};
`;

const ColumnTitle = styled.p`
  grid-row: 1;
  text-transform: uppercase;
  font-size: 0.8em;
`;

const CourseTable = styled.div`
  width: 100%;
  background-color: white;
  padding: 1rem 0;
`;

const Header = styled.div`
  padding: 1rem;
  width: 100%;
  display: flex;
`;

const StyledButton = styled(Button)`
  width: 130px;
  margin: 0.5rem 0;
`;

const AddClassLink = styled(Link)`
  display: flex;
  justify-content: flex-end;
  text-decoration: none;
`;

// Grid columns:
// Class Name
// Rate
// Category
// Description -- hidden in collpasable menu
// Media -- images/videos -- hidden in collapsable menu

// prettier-ignore
// const tempCourses = [
//   { id: '1', name: 'Pottery', category: 'Arts & Crafts', rate: 22, description: '' ,media: ['test'] },
//   { id: '2', name: 'HTML & CSS', category: 'Web Development', rate: 40, description: '' ,media: [] },
//   { id: '3', name: 'Basic Sous Vide', category: 'Cooking', rate: 15, description: '' ,media: [] },
//   { id: '4', name: 'How to play Poker', category: 'Games', rate: 20, description: '' ,media: [] },
// ];

function EditCourses() {
  const dispatch: AppDispatch = useDispatch();

  useEffect(() => {
    dispatch(fetchOwnCourses());
  }, []);

  const courses = useSelector((state: RootState) => state.user.userCourses);
  console.log(courses);
  return (
    <Layout>
      <AddClassLink to={ADD_CLASS_ROUTE}>
        <StyledButton primary>Add Course</StyledButton>
      </AddClassLink>
      <Table>
        <Header>
          <Column flex={2}>
            <ColumnTitle>Class Name</ColumnTitle>
          </Column>
          <Column flex={2}>
            <ColumnTitle>Category</ColumnTitle>
          </Column>
          <Column flex={1}>
            <ColumnTitle>Rate (/hr)</ColumnTitle>
          </Column>
        </Header>
        <CourseTable>
          {courses.map(course => (
            <Row courseInfo={course} />
          ))}
        </CourseTable>
      </Table>
    </Layout>
  );
}

export default EditCourses;
