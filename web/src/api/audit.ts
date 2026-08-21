import { http } from './client';

export const auditApi = {
  ping: () => http.get('/audit/ping'),
};
