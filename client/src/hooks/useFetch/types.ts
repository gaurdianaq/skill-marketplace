import { IError } from '../../global';

export type FetchMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | undefined;

export type ActionType = {
  type: string;
  payload?: any;
  body?: any;
  endpoint?: string;
};

export type StateType = {
  data?: any;
  error?: IError;
  endpoint?: string;
  method?: FetchMethod;
  body?: any;
  isLoading: boolean;
  status?: number;
};

export type AjaxParam = {
  body?: any;
};
