import apiClient from './client';
import { unwrap } from './request';

export const votesApi = {
  results: (boardId) => unwrap(() => apiClient.get(`/api/boards/${boardId}/votes`)),
  cast: (boardId, { optionId, rating, anonymous }) =>
    unwrap(() => apiClient.post(`/api/boards/${boardId}/votes`, {
      optionId,
      ...(rating === undefined ? {} : { rating }),
      anonymous,
    })),
};

export default votesApi;
