import React, { useState } from 'react';
import styled from 'styled-components/macro';
import Button from '../Button';

const Container = styled(Button)`
  align-self: flex-end;
  width: 130px;
  margin: 0.5rem;
`;

type Props = {
  handleClick: () => void;
};

const DeleteCourseButton = ({ handleClick }: Props) => {
  const [confirm, setConfirm] = useState(false);

  const onClick = () => {
    if (confirm) handleClick();
    setConfirm(true);
  };
  return (
    <Container color="hsla(0, 88%, 58%, 1)" primary={confirm} onClick={onClick}>
      {confirm ? <p>Are you sure?</p> : <p>Delete Course</p>}
    </Container>
  );
};

export default DeleteCourseButton;
