import React, { SyntheticEvent } from 'react';
import styled from 'styled-components/macro';
import Card from '../components/Card';
import HomeCard from '../components/HomeCard';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../redux/reducer';
import CategoryDropdown from '../components/CategoryDropdown';
import { useForm } from 'react-hook-form';
// import SelectDropdown from '../components/SelectDropdown';

const Container = styled.main`
  display: flex;
  flex-direction: column;
  padding: 2rem 8rem;
`;

const SearchField = styled.form`
  display: flex;
  padding: 1rem;
  input {
    border-radius: 5px;
    min-height: 30px;
    border: 1px solid #aaa;
    border-right: none;
    border-top-right-radius: 0;
    border-bottom-right-radius: 0;
    padding: 0.5rem;
  }
  select {
    border: 1px solid #aaa;
    border-radius: 5px;
    border-top-left-radius: 0;
    border-bottom-left-radius: 0;
  }
`;

const Results = styled.section`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-gap: 2rem;
  justify-items: center;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 3rem;
`;

function Home() {
  const { register, handleSubmit } = useForm();

  const onSubmit = handleSubmit(form => {
    console.log('onsubmit', form);
  });

  const dispatch = useDispatch();
  const categories = useSelector((state: RootState) => state.app.categories);

  return (
    <Container>
      <h1>Learning for anyone, anywhere</h1>
      <SearchField onSubmit={onSubmit}>
        <input type="text" placeholder="What do you want to learn?" ref={register} name="search" />
        <CategoryDropdown title="All Skills" register={register} name="category" />
      </SearchField>
      <Results>
        {Array(10)
          .fill(null)
          .map(() => (
            <Card h="350px" w="300px" flexDirection="column" align="center">
              <HomeCard
                classTitle="class title"
                instructor="instructor name"
                rate={22}
                rating={3}
              />
            </Card>
          ))}
      </Results>
    </Container>
  );
}

export default Home;
