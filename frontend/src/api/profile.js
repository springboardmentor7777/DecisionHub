import apiClient from './client';
import { unwrap } from './request';

export const profileApi = {
  get: () => unwrap(() => apiClient.get('/api/users/me')),
  update: (profile) => unwrap(() => apiClient.put('/api/users/me', profile)),
  changePassword: (passwordChange) =>
    unwrap(() => apiClient.put('/api/users/me/password', passwordChange)),
};

export default profileApi;
