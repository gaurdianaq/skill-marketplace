import React from 'react';
import ReactDOM from 'react-dom';
import { createGlobalStyle, ThemeProvider } from 'styled-components';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import theme from './style/theme';
import * as serviceWorker from './serviceWorker';
import { Provider } from 'react-redux';
import store from './redux/store';

const GlobalStyle = createGlobalStyle`
body, html{
  max-width: 100vw;
  background-color: ${p => p.theme.backgroundColor};
  color: ${p => p.theme.strokeColor};
  font-family: ${p => p.theme.textFont};
}
*{
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
a {
  cursor: pointer;
  :visited {
    color: ${p => p.theme.strokeColor};
  }
}
.visually-hidden { 
    position: absolute !important;
    height: 1px; 
    width: 1px;
    overflow: hidden;
    clip: rect(1px 1px 1px 1px); /* IE6, IE7 */
    clip: rect(1px, 1px, 1px, 1px);
    white-space: nowrap; /* added line */
}
`;

ReactDOM.render(
  <React.StrictMode>
    <Provider store={store}>
      <BrowserRouter>
        <ThemeProvider theme={theme}>
          <GlobalStyle />
          <App />
        </ThemeProvider>
      </BrowserRouter>
    </Provider>
  </React.StrictMode>,
  document.getElementById('root')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
