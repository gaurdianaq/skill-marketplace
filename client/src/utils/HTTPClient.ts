import { FetchMethod } from '../hooks/useFetch/types';

const LOG_RESULTS = false;

const attachBody = (options: RequestInit, body: any) => {
  const headers = new Headers(options.headers);
  headers.append('Content-Type', 'application/json');
  if (LOG_RESULTS) console.log('Attaching Body:', body);
  return {
    ...options,
    headers,
    body: JSON.stringify(body),
  };
};

const Client = {
  request: async (endpoint: string, method: FetchMethod = 'GET', body?: any) => {
    // call for POST/PUT/PATCH

    let options: RequestInit = { method };
    try {
      if (body) {
        options = attachBody(options, body);
      }
      if (LOG_RESULTS) console.log(`Making ${method} request to route`, endpoint);
      const resp = await fetch(endpoint, options);
      if (LOG_RESULTS) console.log('Response from HTTP Client: ', resp);
      if (!resp.ok) {
        const err = await JSON.stringify({ status: resp.status, message: resp.statusText });
        throw Error(err);
      }
      const data = await resp.json();
      if (LOG_RESULTS) console.log('JSON data from HTTP Client: ', data);
      return data;
    } catch (err) {
      console.error('HTTP Client error:', err);
      err = await JSON.parse(err.message);
      const { status, statusText } = err;
      return { error: { status: status || '500', message: statusText || 'Server error' } };
    }
  },

  // requestNoBody: async (endpoint, method = 'GET') => {
  //   // call for GET/DELETE
  //   const token = localStorage.getItem('token');
  //   const auth = `Bearer: ${token}`;
  //   const headers = {
  //     Authorization: auth,
  //   };
  //   const resp = await fetch(endpoint, { headers });
  //   return resp;
  // },
};
export default Client;
