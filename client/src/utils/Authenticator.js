import HTTPClient from './HTTPClient';

/**
 * @return {boolean}
 */

async function Authenticate(email, password) {
  let result = 200;
  HTTPClient.request('/authentication', 'POST', { strategy: 'local', email, password }).then(
    async resp => {
      console.log(resp.status);
      if (resp.status === 201) {
        await resp.json().then(data => {
          console.log(data);
          localStorage.setItem('token', data.accessToken);
        });
      }
      result = resp.status;
    }
  );
  return result;
}

export default Authenticate;
