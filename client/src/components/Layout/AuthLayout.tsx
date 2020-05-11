import React, { ReactNode } from 'react';
import styled from 'styled-components/macro';
import SplashImg from '../../static/img/undraw_youtube_tutorial_2gn3.svg';

const Container = styled.div`
  display: flex;
  height: calc(100vh - ${p => p.theme.headerHeight});
`;

const FormContainer = styled.div`
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
`;

const ImageContainer = styled.img`
  max-width: 55vw;
  height: 100%;
`;

type Props = {
  children: ReactNode;
};
const AuthLayout = ({ children }: Props) => {
  return (
    <Container>
      <FormContainer>{children}</FormContainer>
      <ImageContainer src={SplashImg} />
    </Container>
  );
};

export default AuthLayout;
