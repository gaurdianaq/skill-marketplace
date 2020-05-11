import React, { useState } from 'react';
import styled from 'styled-components/macro';
import useFetch from '../hooks/useFetch/useFetch';
import Avatar from '../components/Avatar';
import Card from '../components/Card';
import Button from '../components/Button';

const Container = styled.div`
  display: flex;
  padding: 5rem;
`;

const Header = styled.header`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
`;

const Subsection = styled.div`
  padding: 0 20%;
  margin: 1.5rem auto;
`;

const ImagePlaceholder = styled.div`
  height: 400px;
  background-color: blue;
  width: 1000px;
  margin: auto;
  opacity: 0.3;
`;
const ImageCarouselPlaceholder = styled.div`
  height: 100px;
  background-color: red;
  opacity: 0.3;
  width: 1000px;
  margin: auto;
`;

const StyledButton = styled(Button)`
  width: 100px;
`;

const ClassInfo = styled.div``;

type Props = {
  classId: string;
};

function CourseProfile({ classId }: Props) {
  // const {data, error} = useFetch(`endpont/${classId}`)

  return (
    <Container>
      <Card flexDirection="column" p="3rem" w="400px" f={5}>
        <Header>
          <Avatar size={100} />
          <h3>NAME</h3>
          <h1>CLASS TITLE</h1>
        </Header>
        <ImagePlaceholder />
        <ImageCarouselPlaceholder />
        <Subsection>
          <h3>About the Instructor</h3>
          <p>
            Lorem ipsum dolor sit amet consectetur adipisicing elit. Animi at aliquid asperiores,
            velit illum quis dolor eaque dolorem ad perspiciatis distinctio odio accusantium libero,
            explicabo officiis laboriosam, ipsa corrupti aut. Suscipit tenetur, ipsum, corrupti
            saepe aut dolorum asperiores iste magnam dolorem quo officia eaque repudiandae deserunt
            quisquam assumenda sunt accusamus consequuntur dolore velit natus qui a omnis eius
            repellendus. Quis. Corporis provident laudantium iusto ut omnis maxime, voluptates
            labore modi dolorem quod animi quis officiis similique voluptatum asperiores autem a
            quia ullam vel excepturi illo veniam! Est provident vero ipsum?
          </p>
        </Subsection>
        <Subsection>
          <h3>About Class</h3>
          Lorem ipsum dolor, sit amet consectetur adipisicing elit. Quam enim dolorum officiis odio,
          ipsam doloremque maiores quasi ex voluptas commodi maxime ducimus qui similique vel et id
          necessitatibus laboriosam natus? Aliquam porro eos, perferendis fugiat eum blanditiis
          quam! Ea, debitis. Sit unde necessitatibus dignissimos nulla odit, minus voluptatibus esse
          quia facilis cumque eos magnam a commodi. Velit id quia alias! Sequi tenetur, quibusdam
          placeat ad libero adipisci quam quasi impedit ipsa delectus iure praesentium eveniet
          maxime doloribus magni quos maiores quae laudantium aperiam nulla natus mollitia,
          necessitatibus architecto labore! Aliquid? Lorem ipsum dolor sit amet consectetur
          adipisicing elit. Praesentium repudiandae, temporibus in odio fugiat suscipit, alias esse
          vitae perferendis beatae vero aliquid dolorem! Id numquam fuga accusamus corrupti esse
          libero. Nostrum, nobis sed praesentium animi aperiam porro excepturi qui laudantium,
          suscipit delectus odio optio facere doloremque explicabo itaque voluptatibus adipisci!
          Similique sit vel itaque sapiente id sunt aut maiores assumenda! Odio quae assumenda quos
          id accusamus cumque laudantium esse, expedita, impedit harum eos enim fuga rerum adipisci?
          Obcaecati harum voluptatem fugit quaerat perspiciatis nihil fuga dolore nulla dolorem
          eius. Modi. Itaque maxime sequi ad reprehenderit. Repellendus, ea commodi quidem mollitia
          dolorem unde culpa alias illo repellat perspiciatis, possimus, consectetur fuga asperiores
          exercitationem tempora. Recusandae facere magni labore. Aperiam, magnam porro!
        </Subsection>
      </Card>
      <Card h="400px" w="300px" m="3rem" flexDirection="column" center>
        <div>CONTENT</div>
        <StyledButton>Buy course</StyledButton>
      </Card>
    </Container>
  );
}
export default CourseProfile;
