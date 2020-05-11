import React from 'react';
import styled from 'styled-components/macro';

const Container = styled.div<Props>`
  height: ${p => p.size}px;
  width: ${p => p.size}px;
  border-radius: 100%;
  background-color: ${p => p.theme.color.primary};
`;

type Props = {
  size?: number;
  src?: string;
  className?: string;
};

function Avatar({ size = 50, src, className = '' }: Props) {
  return <Container className={className} size={size}></Container>;
}

export default Avatar;
