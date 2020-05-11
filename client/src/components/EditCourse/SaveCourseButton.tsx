import React, { useState, useEffect } from 'react';
import styled from 'styled-components/macro';
import Button from '../Button';

const StyledButton = styled(Button)`
  align-self: flex-end;
  width: 130px;
  margin: 0.5rem;
`;

type Props = {
  disabled: boolean;
  submitted: boolean;
};

const SaveCourseButton = ({ disabled, submitted }: Props) => {
  // const [isSubmitted, setIsSubmitted] = useState(false);
  // useEffect(() => {
  //   if (isSubmitted) return;
  //   if (disabled) {
  //     setIsSubmitted(false);
  //   }
  //   return () => {
  //     setIsSubmitted(false);
  //   };
  // }, [disabled]);
  return (
    <StyledButton type={!submitted ? 'submit' : undefined} primary={!submitted} disabled={disabled}>
      {submitted ? <p>Changes Saved!</p> : <p>Save Changes</p>}
    </StyledButton>
  );
};

export default React.memo(SaveCourseButton);
