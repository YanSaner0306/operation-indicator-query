import { http } from './client';

export const strategyApi = {
  ping: () => http.get('/strategy/ping'),
};
