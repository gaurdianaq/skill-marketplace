module.exports = {
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 2018,
    sourceType: 'module',
    project: './tsconfig.json',
    tsconfigRootDir: __dirname,
  },
  settings: {
    'import/extensions': ['.js', '.jsx', '.ts', '.tsx'],
  },
  extends: [
    'react-app',
    'airbnb-typescript',
    'jsx-a11y/recommended',
    'prettier/@typescript-eslint',
  ],
  plugins: ['jsx-a11y', '@typescript-eslint', 'prettier'],
  rules: {
    semi: 1,
    'react/jsx-filename-extension': [1, { extensions: ['.js', '.jsx'] }],
    'react/prop-types': 1,
    'react/jsx-tag-spacing': 1,
    'no-underscore-dangle': 0,
  },
};
