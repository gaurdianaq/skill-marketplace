import React from 'react';
import styled from 'styled-components/macro';
import athelete from '../images/athelete.jpg';
const Container = styled.div<Props>`
  height: ${(p) => p.size}px;
  width: ${(p) => p.size}px;
  border-radius: 100%;
  background-color: ${(p) => p.theme.color.primary};
`;

type Props = {
  size?: number;
  src?: string;
  className?: string;
  srcImage?: string;
};

function Avatar({ size = 50, srcImage, className = '' }: Props) {
  return (
    <Container className={className} size={size}>
      <img src={athelete} alt="../../public/images/athelete.jpg" />
    </Container>
  );
}

export default Avatar;
