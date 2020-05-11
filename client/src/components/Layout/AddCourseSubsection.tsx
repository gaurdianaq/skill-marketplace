import React, { ReactNode } from 'react';
import styled from 'styled-components/macro';

const Container = styled.section`
  display: flex;
  border-top: 1px solid lightgray;
  border-bottom: 1px solid lightgray;
  padding: 2rem 0;
`;

const TitleContainer = styled.div`
  font-size: 2.3em;
  font-weight: 800;
  flex: 2;
`;

const ContentContainer = styled.div`
  flex: 5;
  display: flex;
  align-items: center;
  justify-content: center;
  > input {
    width: 100%;
    min-height: 1.4em;
    line-height: 2em;
  }
`;

type Props = {
  title: string;
  children: ReactNode;
};

const AddCourseSubsection = ({ title, children }: Props) => {
  return (
    <Container>
      <TitleContainer>{title}</TitleContainer>
      <ContentContainer>{children}</ContentContainer>
    </Container>
  );
};

export default AddCourseSubsection;
