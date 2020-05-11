/* eslint-disable react/require-default-props */
import React from 'react';
import styled from 'styled-components/macro';

type Props = {
  h?: string | number; // height
  w?: string | number; // width
  m?: string | number; // margin
  p?: string | number; // padding
  f?: string | number; // flex
  center?: boolean;
  boxShadow?: string;
  flexDirection?: 'row' | 'column';
  align?: 'center' | 'flex-start' | 'flex-end';
  justify?: 'center' | 'flex-start' | 'flex-end' | 'space-between' | 'space-around';
  isFlex?: boolean;
  className?: string;
  children?: React.ReactNode;
};

const Container = styled.div<Props>`
  display: flex;
  box-shadow: ${p => p.boxShadow || '0px 10px 23px 0px rgba(0, 0, 0, 0.2)'};
  margin: ${p => p.m};
  padding: ${p => p.p};
  flex: ${p => p.f};
  height: ${p => p.h};
  width: ${p => p.w};
  display: ${p => p.isFlex && 'flex'};
  justify-content: ${p => p.justify};
  align-items: ${p => p.align};
  flex-direction: ${p => p.flexDirection};
  ${p => p.center && 'display: flex; justify-content: center; align-items: center;'};
`;

function Card({ children, ...props }: Props) {
  return <Container {...props}>{children}</Container>;
}

export default Card;
