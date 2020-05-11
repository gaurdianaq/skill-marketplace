import { useCallback, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import * as yup from 'yup';

const useYupValidation = () => {
  const useYupValidationResolver = (validationSchema: any) =>
    useCallback(
      async data => {
        try {
          const values = await validationSchema.validate(data, {
            abortEarly: false,
          });

          return {
            values,
            errors: {},
          };
        } catch (errors) {
          return {
            test: 'h',
          };
        }
      },
      [validationSchema]
    );
  return { nothing: 'asdf' };
};

// const validationSchema = useMemo(
//   () =>
//     yup.object({
//       firstName: yup.string().required('Required'),
//       lastName: yup.string().required('Required'),
//     }),
//   []
// );

export default useYupValidation;
