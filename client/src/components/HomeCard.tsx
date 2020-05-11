import React from 'react';
import { Link } from 'react-router-dom';
import styled from 'styled-components/macro';
import Star from './Icons/Star';
import Avatar from './Avatar';

const Content = styled.div`
  padding: 1rem;
  flex: 6;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  & + div {
    flex: 1;
    border-top: 1px solid #aaa;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 0.5rem;
    text-transform: uppercase;
    color: ${p => p.theme.color.gray};
    font-weight: 600;
  }
`;

const StyledAvatar = styled(Avatar)`
  margin: 1rem 0;
`;

const Text = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex: 3;
`;

type Props = {
  instructor: string;
  classTitle: string;
  rating?: number;
  rate: number;
};

function Card({ instructor, classTitle, rating = 0, rate }: Props) {
  const starCount = Math.round(rating);

  return (
    <>
      <Content>
        <StyledAvatar size={95} />
        <Text>
          <p>{instructor}</p>
          <h3 title={classTitle}>
            <Link to="/class">Class</Link>
          </h3>
          <div>
            {Array(5)
              .fill(null)
              .map((star, i) => {
                return <Star isActive={i < starCount} size={12} />;
              })}
          </div>
          <p>{rate}</p>
        </Text>
      </Content>
      <div>Send Message</div>
    </>
  );
}

export default Card;
