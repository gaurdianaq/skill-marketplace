import React, { useRef, useEffect, useState } from 'react';
import styled from 'styled-components/macro';
import { Link } from 'react-router-dom';
import { EDIT_CLASSES_ROUTE } from '../Routes';

type ContainerProps = {
  isLocked: boolean;
};

const Container = styled.nav<ContainerProps>`
  color: ${p => p.theme.color.gray};
  font-size: 1.15em;
  line-height: 1.5em;
  user-select: none;
  min-width: 200px;
  margin: 0 2rem;
  display: flex;
  justify-content: center;
  font-family: 'Sen';
  transition: 0.2s ease-in;

  ul {
    list-style: none;
    position: ${p => (p.isLocked ? 'fixed' : 'relative')};
    top: 5rem;
  }
  li {
    padding: 0.5rem 0;
    min-width: 155px;
  }
  li:hover {
    color: ${p => p.theme.color.primary};
  }
`;

type Props = { sections?: any[] };

function SideNav({ sections }: Props) {
  const [locked, setLocked] = useState(false);
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    const handleScroll = () => {
      if (!ref || !ref.current) return;
      const distanceFromTop = ref.current.getBoundingClientRect().y;
      if (locked && distanceFromTop < 0) return;
      if (!locked && distanceFromTop > 0) return;
      setLocked(distanceFromTop < 0);
    };
    window.addEventListener('scroll', handleScroll);
    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, [locked]);

  return (
    <Container ref={ref} isLocked={locked}>
      <ul>
        <li>Basic Information</li>
        <li>About Me</li>
        <li>About Course</li>
        <li>Contact</li>
        <li>Settings</li>
        <Link to={EDIT_CLASSES_ROUTE}>Class Management</Link>
      </ul>
    </Container>
  );
}

export default SideNav;
