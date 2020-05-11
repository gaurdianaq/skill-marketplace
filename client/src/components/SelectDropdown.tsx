import React, { useState } from 'react';
import styled from 'styled-components';

const Container = styled.div`
  position: relative;
  border: 1px solid black;
  height: 30px;
`;

const OptionContainer = styled.div`
  position: absolute;
  top: 30px;
`;

const Option = styled.div``;

type OptionType = {
  name: string;
  value?: string;
};

type Props = {
  options: OptionType[];
  defaultValue?: string;
  className?: string;
  label?: string;
  multi?: boolean;
};

const SelectDropdown = ({ options, defaultValue, className, multi, label }: Props) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [value, setValue] = useState('');
  const [selectedOptions, setSelectedOptions] = useState(new Set());

  return (
    <Container className={className}>
      <OptionContainer>
        {options.map(option => {
          return <Option>{option.name}</Option>;
        })}
      </OptionContainer>
    </Container>
  );
};

export default SelectDropdown;
