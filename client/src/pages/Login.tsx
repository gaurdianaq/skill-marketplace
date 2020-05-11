import React, { useEffect } from 'react';
import styled from 'styled-components/macro';
import useFetch from '../hooks/useFetch/useFetch';
import { useForm } from 'react-hook-form';
import { useDispatch } from 'react-redux';
import { useHistory, Link } from 'react-router-dom';
import { AppDispatch } from '../redux/store';
import { authenticateToken } from '../redux/UserState/userSlice';
import { BACKEND_LOGIN_ROUTE, REGISTER_ROUTE } from '../Routes';
import Button from '../components/Button';
import FlexDiv from '../components/FlexDiv';
import Layout from '../components/Layout/AuthLayout';
import TextInput from '../components/TextInput';

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

function Login() {
  const { data, error, fetch, isLoading } = useFetch();
  const { handleSubmit, register } = useForm();
  const history = useHistory();
  const dispatch: AppDispatch = useDispatch();

  const onSubmit = handleSubmit(form => {
    const { email, password } = form;
    fetch.post(BACKEND_LOGIN_ROUTE, { body: { email, password } });
  });

  useEffect(() => {
    if (error) console.log(error);
    if (data) {
      dispatch(authenticateToken());
      history.push('/');
    }
  }, [data, error]);

  return (
    <Layout>
      <Container column>
        <StyledForm onSubmit={onSubmit}>
          <TagLine>Let's get you logged in.</TagLine>
          <TextInput row={2} col="1/3" ref={register} label="Email" name="email" type="text" />
          <TextInput
            row={3}
            col="1/3"
            ref={register}
            label="Password"
            name="password"
            type="password"
          />
          <StyledButton primary type="submit">
            Login
          </StyledButton>
          {!!error && <Error>{error.message}</Error>}
          <p style={{ gridColumn: '1/3' }}>
            Don't have an account? Sign up {<Link to={REGISTER_ROUTE}>here</Link>}.
          </p>
        </StyledForm>
      </Container>
    </Layout>
  );
}

export default Login;
