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
  height: ${(p) => p.theme.headerHeight};
  box-shadow: 0px 3px 7px 0px rgba(0, 0, 0, 0.1);
  display: flex;
  width: 100%;
  justify-content: space-between;
  font-family: 'Montserrat', sans-serif;
`;

const Nav = styled.nav`
  display: flex;
  align-items: center;
  padding-right: 3rem;
  > * {
    padding: 0 1rem;
    text-transform: uppercase;
    text-decoration: none;
    color: ${(p) => p.theme.strokeColor};
    font-family: ${(p) => p.theme.textFont};
  }
`;

const Logo = styled.div`
  height: 100%;
  width: 200px;
  color: black;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: 700;
  font-size: 22px;
`;

const LogoLink = styled.link`
  text-decoration: none;
`;

const LogInButton = styled.button`
  height: 47px;
  width: 137px;
  border-radius: 5px;
  border: 1px solid #3fcdcf;
  background: transparent;
  color: #3fcdcf;
  padding: 14px 28px;
`;

const SignUpButton = styled.button`
  height: 47px;
  width: 137px;
  border-radius: 5px;
  background-color: #3fcdcf;
  border: none;
  color: white;
`;

function Header() {
  const loggedIn = !!useSelector((state: RootState) => state.user.userData.firstName);

  return (
    <Container>
      <Logo>
        <Link to="/" style={{ textDecoration: 'none' }}>
          Logo.
        </Link>
      </Logo>
      <Nav>
        {loggedIn ? (
          <>
            <Link to={EDIT_PROFILE_ROUTE}>
              <Avatar size={30} />
            </Link>
          </>
        ) : (
          <>
            <a href={LOGIN_ROUTE}>
              <LogInButton>LOGIN</LogInButton>
            </a>
            <a href={REGISTER_ROUTE}>
              <SignUpButton>SIGN UP</SignUpButton>
            </a>
          </>
        )}
      </Nav>
    </Container>
  );
}

export default Header;
