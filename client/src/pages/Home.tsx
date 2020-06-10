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

  @media only screen and (max-width: 950px) {
    padding-left: 2%;
    padding-right: 2%;
  }
`;

const SearchField = styled.form`
  display: flex;
  width: 100%;
  padding: 1rem;
  align-items: stretch;
  input {
    border-radius: 5px;
    min-height: 30px;
    border: 1px solid #aaa;
    border-top-right-radius: 0;
    border-bottom-right-radius: 0;
    border-right: none;
    padding: 2px 23px 2px 30px;
    max-width: 500px;
    min-width: 215px;
    flex-grow: 4;
  }
  input.middle:focus {
    outline-width: 0;
  }
  select {
    border-top-right-radius: 0%;
    border-bottom-right-radius: 0%;
    -webkit-appearance: none;
    -webkit-border-top-left-radius: 0px;
    -webkit-border-bottom-left-radius: 0px;
    -webkit-border-top-right-radius: 5px;
    -webkit-border-bottom-right-radius: 5px;
    padding: 1.2em 3em 1.3em 1.5em;
    background-image: url("data:image/svg+xml;utf8,<svg version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' width='24' height='24' viewBox='0 0 24 24'><path fill='%23444' d='M7.406 7.828l4.594 4.594 4.594-4.594 1.406 1.406-6 6-6-6z'></path></svg>");
    background-position: 100% 50%;
    background-repeat: no-repeat;
    flex-grow: 1;
    min-width: 100px;
    max-width: 200px;
  }
`;

const Results = styled.section`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  grid-gap: 2rem;
  justify-items: center;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 3rem;
`;

const Search = styled.div`
  position: relative;
  display: flex;
  min-width: 50px;
  align-content: stretch;
  align-items: stretch;
`;

const SearchIcon = styled.img`
  position: absolute;
  top: 30px;
  left: 25px;
  width: 14px;
  fill: blue;
`;

function Home() {
  const { register, handleSubmit } = useForm();

  const onSubmit = handleSubmit((form) => {
    console.log('onsubmit', form);
  });

  const dispatch = useDispatch();
  const categories = useSelector((state: RootState) => state.app.categories);
  const placeHoloders = [
    '../../../public/images/athelete.jpg',
    '../../public/images/athelete.jpg',
    '../public/images/athelete.jpg',
    '../../../../public/images/athelete.jpg',
    '../../../public/images/athelete.jpg',
    '../../../public/images/athelete.jpg',
    '../../../public/images/athelete.jpg',
    '../../../public/images/athelete.jpg',
    '../../../public/images/athelete.jpg',
    '../../../public/images/athelete.jpg',
    '../../../public/images/athelete.jpg',
    '../',
  ];
  return (
    <Container>
      <h1>Learning for anyone, anywhere</h1>
      <Search>
        <SearchField onSubmit={onSubmit}>
          <SearchIcon src="data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgdmlld0JveD0iMCAwIDU2Ljk2NiA1Ni45NjYiIHN0eWxlPSJlbmFibGUtYmFja2dyb3VuZDpuZXcgMCAwIDU2Ljk2NiA1Ni45NjY7IiB4bWw6c3BhY2U9InByZXNlcnZlIiB3aWR0aD0iMTZweCIgaGVpZ2h0PSIxNnB4Ij4KPHBhdGggZD0iTTU1LjE0Niw1MS44ODdMNDEuNTg4LDM3Ljc4NmMzLjQ4Ni00LjE0NCw1LjM5Ni05LjM1OCw1LjM5Ni0xNC43ODZjMC0xMi42ODItMTAuMzE4LTIzLTIzLTIzcy0yMywxMC4zMTgtMjMsMjMgIHMxMC4zMTgsMjMsMjMsMjNjNC43NjEsMCw5LjI5OC0xLjQzNiwxMy4xNzctNC4xNjJsMTMuNjYxLDE0LjIwOGMwLjU3MSwwLjU5MywxLjMzOSwwLjkyLDIuMTYyLDAuOTIgIGMwLjc3OSwwLDEuNTE4LTAuMjk3LDIuMDc5LTAuODM3QzU2LjI1NSw1NC45ODIsNTYuMjkzLDUzLjA4LDU1LjE0Niw1MS44ODd6IE0yMy45ODQsNmM5LjM3NCwwLDE3LDcuNjI2LDE3LDE3cy03LjYyNiwxNy0xNywxNyAgcy0xNy03LjYyNi0xNy0xN1MxNC42MSw2LDIzLjk4NCw2eiIgZmlsbD0iIzAwMDAwMCIvPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8L3N2Zz4K" />

          <input
            type="text"
            placeholder="What do you want to learn?"
            ref={register}
            name="search"
          />
          <CategoryDropdown title="All Skills" register={register} name="category" />
        </SearchField>
      </Search>

      <Results>
        {placeHoloders.map(() => (
          <Card h="350px" w="300px" flexDirection="column" align="center">
            <HomeCard classTitle="class title" instructor="instructor name" rate={22} rating={3} />
          </Card>
        ))}
      </Results>
    </Container>
  );
}

export default Home;
