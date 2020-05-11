import React, { useState, useEffect, SyntheticEvent } from 'react';
import styled from 'styled-components/macro';
import CategoryDropdown from '../CategoryDropdown';
import { useSelector } from 'react-redux';
import { RootState } from '../../redux/reducer';

const Container = styled.div`
  width: fit-content;
  justify-self: flex-start;
`;

const Text = styled.p<{ editable: boolean }>`
  width: fit-content;
  padding: 0.25rem;
  border-radius: 5px;
  :hover {
    ${p => (p.editable ? 'background-color: darkgray' : '')};
  }
`;

type InputSelectRef = React.Ref<HTMLSelectElement | HTMLInputElement>;
type Props = {
  enabled: boolean;
  defaultValue: React.ReactText;
  isCategory?: boolean;
  name: string;
  register: InputSelectRef;
};

type InputSelectProps = {
  isDropdown: boolean;
  defaultValue: React.ReactText;
  name: string;
  register: InputSelectRef;
};
//  'enabled' props allows:
// - highlight-on-hover effect
// - change cell to input on click

// 'isInput' tracks input vs 'p' state

// Text is plain paragraph unless Cell is enabled
// When cell is enabled, Text has hover effect and can turn into input

const InputOrDropdown = (props: InputSelectProps) => {
  const { isDropdown, defaultValue, name, register } = props;
  if (isDropdown) {
    return (
      <CategoryDropdown
        register={register as React.Ref<HTMLSelectElement>}
        name={name}
        defaultValue={defaultValue}
      />
    );
  }
  return (
    <input
      defaultValue={defaultValue}
      type="text"
      ref={register as React.Ref<HTMLInputElement>}
      name={name}
    />
  );
};

const EditableCell = ({ enabled, defaultValue, isCategory, name, register }: Props) => {
  // const { enabled, defaultValue, isCategory } = props;
  const [isInput, setIsInput] = useState(false);

  useEffect(() => {
    if (!enabled) {
      setIsInput(false);
    }
  }, [enabled]);

  const handleClick = (e: SyntheticEvent) => {
    if (!enabled) return;
    e.stopPropagation();
    setIsInput(true);
  };

  return (
    <Container onClick={handleClick}>
      {isInput ? (
        <InputOrDropdown
          isDropdown={!!isCategory}
          name={name}
          defaultValue={defaultValue}
          register={register}
        />
      ) : (
        <Text editable={enabled}>{defaultValue}</Text>
      )}
    </Container>
  );
};

export default EditableCell;
