import React, { useState } from 'react';
import styled from 'styled-components/macro';
import Layout from '../components/Layout/ProfilePageLayout';
import Subsection from '../components/Layout/AddCourseSubsection';
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../redux/reducer';
import { useForm } from 'react-hook-form';
import { postCourse } from '../redux/UserState/userSlice';
import { CATEGORIES } from '../Constants';
import { ICourse } from '../global';
import { useHistory } from 'react-router-dom';
import { EDIT_CLASSES_ROUTE } from '../Routes';

// const Container = styled.div``;

const CategoryGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-gap: 1rem;
`;

const CategoryContainer = styled.div<{ selected: boolean }>`
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  width: 250px;
  height: 250px;
  z-index: 1;
  font-size: ${p => (p.selected ? '1.07' : '1')}em;
  :before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    height: 100%;
    width: 100%;
    z-index: 2;
    ${p => (p.selected ? `border: 3.5px solid ${p.theme.color.primary}` : '')};
  }
  :after {
    transition: 0.25s ease-in;
    content: '';
    position: absolute;
    background-color: black;
    top: 3.5px;
    left: 3.5px;
    height: 100%;
    width: 100%;
    z-index: 2;
    opacity: ${p => (p.selected ? 0.25 : 0.4)};
  }
  :hover {
    :after {
      opacity: 0.25;
    }
    font-size: 1.07em;
  }
`;

const CategoryLabel = styled.h2`
  z-index: 3;
  font-weight: 600;
  font-size: 2.4em;
  width: 100%;
  text-align: center;
  color: white;
  user-select: none;
  transition: 0.25s ease-out;
`;

const CategoryImg = styled.img`
  position: absolute;
  top: 3.5px;
  left: 3.5px;
`;

const AddCourse = () => {
  const dispatch = useDispatch();
  const categories = useSelector((state: RootState) => state.app.categories);
  const history = useHistory();
  const { handleSubmit, register } = useForm();
  const [selectedCategory, setCategory] = useState('');

  const onSubmit = (data: Omit<ICourse, 'media'>) => {
    const formData: Partial<ICourse> = { ...data, category: selectedCategory, media: [] };
    dispatch(postCourse(formData as ICourse));
    history.push(EDIT_CLASSES_ROUTE);
  };

  return (
    <Layout>
      <form onSubmit={handleSubmit(onSubmit)}>
        <Subsection title="Category">
          <CategoryGrid>
            {CATEGORIES.map(category => (
              <CategoryContainer
                key={category.name}
                selected={category.name === selectedCategory}
                onClick={() => setCategory(category.name)}
              >
                <CategoryImg src={category.img} />
                <CategoryLabel>{category.name}</CategoryLabel>
              </CategoryContainer>
            ))}
          </CategoryGrid>
        </Subsection>
        <Subsection title="Title">
          <input type="text" name="name" ref={register} />
        </Subsection>
        <Subsection title="Description">
          <input type="text" name="description" ref={register} />
        </Subsection>
        <Subsection title="Hourly Rate (USD)">
          <input type="number" name="rate" ref={register} />
        </Subsection>
        {/* <Subsection title="Media">
          <input type="file" name="media" multiple ref={register} />
        </Subsection> */}
        <input type="submit" />
      </form>
    </Layout>
  );
};

export default AddCourse;
