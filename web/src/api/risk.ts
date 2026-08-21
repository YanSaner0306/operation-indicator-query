import { http } from './client';

export const riskApi = {
  ping: () => http.get('/risk/ping'),
};
