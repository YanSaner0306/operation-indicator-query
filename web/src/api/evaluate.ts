import { http } from './client';

export const evaluateApi = {
  ping: () => http.get('/evaluate/ping'),
};
