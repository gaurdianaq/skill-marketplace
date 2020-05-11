import React from 'react';
import styled, { DefaultTheme } from 'styled-components/macro';

type Props = {
  primary?: boolean;
  disabled?: boolean;
  color?: string;
  type?: 'submit';
  onClick?: (x?: any) => void;
  className?: string;
  children: React.ReactNode;
  loadingState?: React.ReactNode;
};

const Container = styled.button.attrs((props: Props & { theme: DefaultTheme }) => {
  if (props.disabled) {
    return {
      bgc: 'white',
      textColor: 'lightgray',
      borderColor: 'lightgray',
    };
  }
  if (props.primary) {
    return {
      bgc: props.color || props.theme.color.primary,
      textColor: 'white',
      borderColor: props.color || props.theme.color.primary,
    };
  }
  if (!props.primary) {
    return {
      bgc: 'white',
      textColor: props.color || props.theme.color.primary,
      borderColor: props.color || props.theme.color.primary,
    };
  }
})<Props>`
  border: 1.5px solid ${p => p.borderColor};
  background-color: ${p => p.bgc};
  color: ${p => p.textColor};
  display: flex;
  box-shadow: 0px 3px 4px 0px rgba(0, 0, 0, 0.2);
  justify-content: center;
  align-items: center;
  border-radius: 5px;
  padding: 0.5rem;
  font-weight: 600;
  user-select: none;
  cursor: pointer;
  transition: 0.1s linear;
  font-size: 0.75em;
  text-transform: uppercase;
  outline: none;
  :active {
    transform: translateY(1.5px);
    box-shadow: 0px 1px 2px 0px rgba(0, 0, 0, 0.2);
  }
`;

// primary - color outline - color background - white text
// not primary  - color outline, white background - color text
// color - define color
// disabled - gray outline, white background, gray text

function Button({ children, loadingState, ...props }: Props) {
  return (
    <Container {...props}>
      {!!loadingState && loadingState}
      {children}
    </Container>
  );
}

export default Button;
