import apiClient from './client';
import { unwrap } from './request';

export const CATEGORIES = ['Career', 'Education', 'Technology', 'Travel', 'Finance', 'Lifestyle'];
export const POLL_TYPES = ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'RATING'];

export const pollLabel = (pollType) => ({
  SINGLE_CHOICE: 'Single choice',
  MULTIPLE_CHOICE: 'Multiple choice',
  RATING: 'Rating',
}[pollType] || pollType);

export const boardsApi = {
  listPublic: () => unwrap(() => apiClient.get('/api/boards/public')),
  listMine: () => unwrap(() => apiClient.get('/api/boards/mine')),
  get: (id) => unwrap(() => apiClient.get(`/api/boards/${id}`)),
  create: (board) => unwrap(() => apiClient.post('/api/boards', board)),
  update: (id, board) => unwrap(() => apiClient.put(`/api/boards/${id}`, board)),
  remove: (id) => unwrap(() => apiClient.delete(`/api/boards/${id}`)),
  addOption: (id, option) => unwrap(() => apiClient.post(`/api/boards/${id}/options`, option)),
};

export default boardsApi;
