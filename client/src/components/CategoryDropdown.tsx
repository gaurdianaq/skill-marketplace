import React from 'react';
import { useSelector } from 'react-redux';
import { RootState } from '../redux/reducer';

type Props = {
  defaultValue?: React.ReactText;
  title?: string;
  name?: string;
  register: React.Ref<HTMLSelectElement>;
  onChange?: () => void;
};

const CategoryDropdown = (props: Props) => {
  const { title, defaultValue, onChange, name, register } = props;
  const categories = useSelector((state: RootState) => state.app.categories);

  return (
    <select name={name} id="" defaultValue={defaultValue} ref={register} onChange={onChange}>
      {!!title && <option value="">{title}</option>}
      {categories.map(category => {
        return <option value={category}>{category}</option>;
      })}
    </select>
  );
};

export default CategoryDropdown;
