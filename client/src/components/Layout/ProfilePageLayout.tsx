import React, { useEffect, useRef, useCallback, useState, ReactNode } from 'react';
import styled from 'styled-components/macro';
import SideNav from '../SideNav';
import Card from '../Card';
import FlexDiv from '../FlexDiv';

const Container = styled.div`
  display: flex;
`;

const StyledCard = styled(Card)`
  min-height: calc(100vh - ${p => p.theme.headerHeight});
`;

type Props = {
  children: ReactNode;
};

function ProfilePageLayout({ children }: Props) {
  const [sections, setSections] = useState([]);

  // SideNav needs click listeners that scroll to each section
  // SideNav should receive and object of {<sectionTitle>: <clickHandler>}
  // Each section needs to be mapped over with refs
  // State Object keeps track of refs and click listeners

  return (
    <Container>
      <FlexDiv>
        <SideNav sections={sections} />
      </FlexDiv>
      <FlexDiv>
        <StyledCard w="1200px" m="0 0 150px 100px" p="2rem 4rem" flexDirection="column">
          {children}
        </StyledCard>
      </FlexDiv>
    </Container>
  );
}

export default ProfilePageLayout;
