import React, { useRef, SyntheticEvent } from 'react';
import styled from 'styled-components/macro';
import useUpload from '../../hooks/useUpload/useUpload';

const Container = styled.div`
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-gap: 1rem;
  border: 1px solid lightgray;
  padding: 1rem;
`;

const InputContainer = styled.div`
  width: 125px;
  height: 125px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 4px dashed lightgray;
  border-radius: 8px;
`;

const ImageThumbnail = styled(InputContainer)`
  border: none;
`;

const Image = styled.img`
  width: 125px;
  height: 125px;
  object-fit: contain;
`;

type Props = {
  media?: string[];
  name: 'media';
  setValue: any;
  getValue: any;
  onComplete?: (x: FileList) => FileList | string[];
  register: any;
};

const MediaContainer = (props: Props) => {
  const { media = [], setValue, getValue } = props;
  const { addThumbnail, files, loading, resetState } = useUpload(media);
  const uploadRef = useRef<HTMLInputElement>(null);

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    const { files } = e.dataTransfer;
    updateForm(files);
    // return ui state to normal
  };

  const handleInput = (e: React.ChangeEvent) => {
    const { files } = e.target as HTMLInputElement;
    updateForm(files as FileList);
  };

  const updateForm = (newImages: FileList) => {
    addThumbnail(newImages);
    const media = getValue().media;
    setValue('media', [...media, ...Array.from(newImages)]);
  };

  const stopDefault = (e: SyntheticEvent) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const dragEnter = (e: SyntheticEvent) => {
    e.preventDefault();
    e.stopPropagation();
    console.log(e);
    // change ui state to show file is entered/container is hovered over
  };

  const uploadClick = () => {
    if (!uploadRef?.current) return;
    uploadRef.current.click();
  };

  return (
    <>
      <Container onDragEnter={dragEnter} onDragOver={stopDefault} onDrop={handleDrop}>
        {!!files.length &&
          files.map(img => {
            return (
              <ImageThumbnail>
                <Image src={img} />
              </ImageThumbnail>
            );
          })}
        <InputContainer onClick={uploadClick}>
          <label htmlFor="media">Upload</label>
          <input
            type="file"
            name="media"
            ref={uploadRef}
            onChange={handleInput}
            className="visually-hidden"
            multiple
          />
        </InputContainer>
      </Container>
    </>
  );
};

export default MediaContainer;

/**
 * 1. Container listens to images dragged on and uploaded
 * 2. useUpload makes a request to the backend containing userId and img in dataURL format
 * 3. backend makes the POST request to Cloudinary in this format:
 *    - timestamp: 1315060510 (valid for 1hr)
 *    - public_id: sample_image (image name)
 *    - api_key: 1234 (public api key)
 *    - eager: w_400,h_300,c_pad|w_260,h_200,c_crop
 *    - file: https://www.example.com/sample.jpg (in this case, the dataURL/base64 encoding)
 *    - signature: bfd09f95f331f558cbd1320e67aa8d488770583e
 * 3b. Additional options found here: https://cloudinary.com/documentation/image_upload_api_reference
 *    - Other options to consider, folder, tags (userId). folder path can also be defined in public_id
 * 3c. Signature is a SHA-1 encrypted string (alphabetically ordered parameter keys)
 *    eg. eager=w_400,h_300,c_pad|w_260,h_200,c_crop&public_id=sample_image&timestamp=1315060510<APISECRETKEY>
 *
 * 4. After all the options are gathered, they are URL encoded in the body of the request
 *  'file=https://www.example.com/sample.jpg&eager=w_400,h_300,c_pad|w_260,h_200,c_crop&timestamp=173719931&api_key=436464676&signature=a788d68f86a6f868af'
 * 5. Request returns eager transformed img urls that can be used as thumbnails to notify user of upload
 * 5b. Alternatively full image size thumbnails can be temporarily created locally
 *
 *
 * Frontend State -- all managed by useUpload/Redux
 *
 *  1. Preexisting images are loaded into useUpload's files as URLs
 *  2. As images are added, they are converted to base64 to provide an image preview
 *  3. When the form is submitted, the raw FileList is submitted to the backend via Redux
 *  4. In the Redux dispatch, the post request is made and gets the updated course back
 *  5. Redux finds the correct course and updates
 *
 *  Image Deletion --
 *  1. If images are marked for deletion, Redux handles the post request and returns a 2xx status code
 *  2. Images are found in the course and removed.
 */
