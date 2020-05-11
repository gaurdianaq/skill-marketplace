import React, { useState, useEffect } from 'react';
import styled from 'styled-components/macro';
import { useForm } from 'react-hook-form';
import { useHistory, Link } from 'react-router-dom';
import { USERS_ROUTE, LOGIN_ROUTE } from '../Routes';
import TextInput from '../components/TextInput';
import useFetch from '../hooks/useFetch/useFetch';
import Button from '../components/Button';
import FlexDiv from '../components/FlexDiv';
import Layout from '../components/Layout/AuthLayout';

const Container = styled(FlexDiv)`
  height: 100%;
  justify-content: flex-start;
  align-items: center;
`;

const Error = styled.p`
  color: red;
`;

const StyledForm = styled.form`
  display: grid;
  grid-template-columns: 1fr 1fr;
  /* height: 50%; */
  max-width: 500px;
  margin: auto 0;
  padding: 4rem 2rem;
`;

const TagLine = styled.h1`
  grid-column: 1/3;
`;

const StyledButton = styled(Button)`
  grid-column: 1/3;
  margin: 0.75rem;
`;

type FormT = {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
};

function Register() {
  const { data, error, fetch, isLoading } = useFetch();
  const history = useHistory();
  const { handleSubmit, register } = useForm();

  const onSubmit = handleSubmit(form => {
    const { email, password, firstName, lastName } = form;
    fetch.post(USERS_ROUTE, {
      body: { email, password, first_name: firstName, last_name: lastName },
    });
  });

  useEffect(() => {
    // some kind of snackbar
    if (error) console.log(error);
    if (data) {
      history.push('/');
    }
  }, [data, error]);

  return (
    <Layout>
      <Container column>
        <StyledForm onSubmit={onSubmit}>
          <TagLine>
            Looking to start learning or making extra money at home? Let's get started here.
          </TagLine>
          <TextInput row={2} col="1/3" ref={register} label="Email" name="email" type="text" />
          <TextInput
            row={3}
            col="1/2"
            ref={register}
            label="First Name"
            name="firstName"
            type="text"
          />
          <TextInput
            row={3}
            col="2/3"
            ref={register}
            label="Last Name"
            name="lastName"
            type="text"
          />
          <TextInput
            row={4}
            col="1/3"
            ref={register}
            label="Password"
            name="password"
            type="password"
          />
          <StyledButton primary type="submit">
            Sign up
          </StyledButton>
          {!!error && <Error>{error.message}</Error>}
          <p style={{ gridColumn: '1/3' }}>
            Already have an account? Log in {<Link to={LOGIN_ROUTE}>here</Link>}.
          </p>
        </StyledForm>
      </Container>
    </Layout>
  );
}

export default Register;
