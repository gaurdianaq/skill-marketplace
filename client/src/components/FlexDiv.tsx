import React, { ReactNode } from 'react';
import styled from 'styled-components/macro';

const Container = styled.div<Props>`
  display: flex;
  flex-direction: ${p => (p.column ? 'column' : 'row')};
  margin: ${p => p.m};
  padding: ${p => p.p};
`;

type Props = {
  className?: string;
  m?: string | number;
  p?: string | number;
  column?: boolean;
  children: ReactNode;
};

const FlexDiv = (props: Props) => {
  return (
    <Container column={props.column} className={props.className}>
      {props.children}
    </Container>
  );
};

export default FlexDiv;
