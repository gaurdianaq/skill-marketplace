import React, { useState, SyntheticEvent } from 'react';
import styled from 'styled-components/macro';
import useFetch from '../hooks/useFetch/useFetch';
import { BACKEND_LOGIN_ROUTE } from '../Routes';
import { useHistory } from 'react-router-dom';

const Container = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
`;

const InputContainer = styled.div`
  display: flex;
`;

type FormT = {
  email: string;
  password: string;
};

function LoginForm() {
  const [formValue, setFormValue] = useState<FormT>({ email: '', password: '' });
  const { data, error: fetchError, fetch, isLoading } = useFetch();
  const history = useHistory();
  // TODO Display error messages to the user

  const handleSubmit = async (e: SyntheticEvent) => {
    e.preventDefault();
    const { email, password } = formValue;
    await fetch.post(BACKEND_LOGIN_ROUTE, { body: { email, password } });
    if (!fetchError) history.push('/');
    else console.error(fetchError);
  };

  const handleChange = (e: SyntheticEvent) => {
    const { name, value } = e.target as typeof e.target & {
      name: 'email' | 'password';
      value: 'string';
    };
    setFormValue({ ...formValue, [name]: value });
  };
  return (
    <Container>
      <form onSubmit={handleSubmit} onChange={handleChange}>
        <InputContainer>
          <label htmlFor="">Email</label>
          <input type="text" name="email" />
        </InputContainer>
        <InputContainer>
          <label htmlFor="password">Password</label>
          <input type="password" name="password" />
        </InputContainer>
        <input type="submit" />
      </form>
    </Container>
  );
}

export default LoginForm;
