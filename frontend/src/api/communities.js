import apiClient from './client';
import { unwrap } from './request';

export const COMMUNITY_CATEGORIES = [
  'CAREER',
  'EDUCATION',
  'TECHNOLOGY',
  'TRAVEL',
  'FINANCE',
  'LIFESTYLE',
  'OTHER',
];

export const communitiesApi = {
  list: () => unwrap(() => apiClient.get('/api/communities')),
  create: (community) => unwrap(() => apiClient.post('/api/communities', community)),
  join: (id) => unwrap(() => apiClient.post(`/api/communities/${id}/members`)),
  leave: (id) => unwrap(() => apiClient.delete(`/api/communities/${id}/members/me`)),
  members: (id) => unwrap(() => apiClient.get(`/api/communities/${id}/members`)),
};

export default communitiesApi;
