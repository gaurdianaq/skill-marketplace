/* eslint-disable react/jsx-props-no-spreading */
import React from 'react';
import styled from 'styled-components/macro';

type ContainerProps = {
  col?: number | string;
  row?: number | string;
};

const Container = styled.div<ContainerProps>`
  grid-column: ${p => p.col || 'auto'};
  grid-row: ${p => p.row || 'auto'};
  display: flex;
  flex-direction: column;
  padding: 0.5rem;
  & .error {
    font-size: 0.85em;
    color: red;
  }
`;

const StyledLabel = styled.label`
  text-transform: uppercase;
  color: #aaa;
  font-weight: 400;
  font-size: 0.85em;
  padding-bottom: 0.5rem;
`;

const StyledInput = styled.input`
  font-size: 1em;
  font-weight: 600;
  min-height: 3em;
  padding: 0 0.5rem;
  outline: none;
  border: 1px solid #aaa;
  border-radius: 5px;

  /*Removes spinner from number inputs */

  ::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
  ::-webkit-outer-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
  :focus {
    border-color: ${p => p.theme.color.primary};
    border-width: 1.5px;
  }
`;

type Props = {
  label: string;
  row?: number | string;
  col?: number | string;
  name: string;
  value?: string;
  type: string;
};

const TextInput = React.forwardRef((props: Props, ref: any) => {
  const { row, col, name, label, value, type } = props;
  return (
    <Container row={row} col={col}>
      <StyledLabel htmlFor={name}>{label}</StyledLabel>
      <StyledInput type={type} ref={ref} name={name} defaultValue={value} />
    </Container>
  );
});

export default TextInput;
