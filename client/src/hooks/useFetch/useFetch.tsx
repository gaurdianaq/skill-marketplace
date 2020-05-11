import { useReducer, useEffect, Reducer } from 'react';
import Client from '../../utils/HTTPClient';
import { ActionType, StateType, AjaxParam } from './types';

const initialState: StateType = {
  data: '',
  error: undefined,
  endpoint: '',
  method: undefined,
  body: '',
  isLoading: false,
  status: undefined,
};

const reducer = (state: StateType, action: ActionType): StateType => {
  const { type, payload, body, endpoint } = action;
  switch (type) {
    case 'GET_ENDPOINT':
      return { ...state, endpoint, method: 'GET' };
    case 'POST_ENDPOINT':
      return { ...state, body, endpoint, method: 'POST' };
    case 'PUT_ENDPOINT':
      return { ...state, body, endpoint, method: 'PUT' };
    case 'DELETE_ENDPOINT':
      return { ...state, endpoint, method: 'DELETE' };
    case 'SET_DATA':
      return { ...state, data: payload, isLoading: false, endpoint: '' };
    case 'FETCHING':
      return { ...state, isLoading: true, error: undefined };
    case 'ERROR': {
      const { error } = payload;
      return { ...state, error, isLoading: false, endpoint: '' };
    }
    default:
      return { ...state };
  }
};

const useFetch = () => {
  const [state, dispatch] = useReducer<Reducer<StateType, ActionType>>(reducer, initialState);
  const { data, isLoading, error, endpoint, method, body } = state;

  useEffect(() => {
    let isMounted = true;
    const fetchData = async () => {
      console.log('fetching...');
      if (!endpoint || isLoading) return;
      dispatch({ type: 'FETCHING' });
      const resp = await Client.request(endpoint, method, body);
      console.log('Data received in useFetch: ', resp, isMounted);
      if (resp.error && isMounted) {
        console.log('Setting error');
        dispatch({ type: 'ERROR', payload: resp });
      } else if (isMounted) {
        console.log('Setting data in useFetch...', resp);
        dispatch({ type: 'SET_DATA', payload: resp });
      }
    };

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [endpoint, method, body]);

  const get = (endpoint: string) => {
    dispatch({ type: 'GET_ENDPOINT', endpoint });
  };
  const post = (endpoint: string, options?: AjaxParam) => {
    const body = options?.body;
    dispatch({ type: 'POST_ENDPOINT', endpoint, body });
  };
  const put = (endpoint: string, options: AjaxParam) => {
    const body = options?.body;
    dispatch({ type: 'PUT_ENDPOINT', endpoint, body });
  };

  const del = (endpoint: string, options: AjaxParam) => {
    const body = options?.body;
    dispatch({ type: 'DELETE_ENDPOINT', endpoint, body });
  };

  const fetch = {
    get,
    post,
    put,
    del,
  };

  return { data, error, fetch, isLoading };
};

export default useFetch;
