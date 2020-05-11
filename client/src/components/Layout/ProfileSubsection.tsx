import React, { ReactNode, ReactElement } from 'react';
import styled from 'styled-components/macro';

type FormT = {
  rows: number;
};

const StyledForm = styled.div<FormT>`
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: repeat(${p => p.rows}, minmax(40px, 1fr));
  align-items: center;
  justify-content: flex-start;
  padding: 1rem 4rem;
  border-bottom: 1px solid ${p => p.theme.changeOpacity('#cccccc', 50)};
`;

const SubsectionTitle = styled.h2`
  grid-row: 1;
  grid-column: 1/3;
`;

type Props = {
  children: ReactElement | ReactElement[];
  title: string;
  // submitHandler: (x: any) => void;
};

function ProfileSubsection({ children, title }: Props) {
  const numOfRows = Math.ceil(React.Children.count(children) / 2);

  return (
    <StyledForm rows={numOfRows}>
      <SubsectionTitle>{title}</SubsectionTitle>
      {children}
    </StyledForm>
  );
}

export default ProfileSubsection;
