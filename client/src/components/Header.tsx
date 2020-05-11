import React from 'react';
import styled from 'styled-components/macro';
import { Link } from 'react-router-dom';
import Avatar from './Avatar';
import {
  EDIT_PROFILE_ROUTE,
  HOME_ROUTE,
  REGISTER_ROUTE,
  LOGIN_ROUTE,
  BACKEND_LOGOUT_ROUTE,
} from '../Routes';
import { useSelector } from 'react-redux';
import { RootState } from '../redux/reducer';

const Container = styled.header`
  height: ${p => p.theme.headerHeight};
  box-shadow: 0px 3px 7px 0px rgba(0, 0, 0, 0.1);
  display: flex;
  width: 100%;
  justify-content: space-between;
`;

const Nav = styled.nav`
  display: flex;
  align-items: center;
  padding-right: 3rem;
  > * {
    padding: 0 1rem;
    text-transform: uppercase;
    text-decoration: none;
    color: ${p => p.theme.strokeColor};
    font-family: ${p => p.theme.textFont};
  }
`;

const Logo = styled.div`
  height: 100%;
  width: 200px;
  background-color: pink;
  color: black;
  display: flex;
  justify-content: center;
  align-items: center;
`;

function Header() {
  const loggedIn = !!useSelector((state: RootState) => state.user.userData.firstName);

  return (
    <Container>
      <Logo>
        <Link to="/">LOGO</Link>
      </Logo>
      <Nav>
        <Link to="/">Browse</Link>
        {/* <a href="#">Inbox</a> */}
        {loggedIn ? (
          <>
            <a href="#">Inbox</a>
            <Link to={EDIT_PROFILE_ROUTE}>
              <Avatar size={30} />
            </Link>
            <Link to={BACKEND_LOGOUT_ROUTE}>Logout</Link>
          </>
        ) : (
          <>
            <Link to={REGISTER_ROUTE}>Register</Link>
            <Link to={LOGIN_ROUTE}>Login</Link>
          </>
        )}
      </Nav>
    </Container>
  );
}

export default Header;
