import apiClient from './client';
import { unwrap } from './request';

export const commentsApi = {
  list: (boardId) => unwrap(() => apiClient.get(`/api/boards/${boardId}/comments`)),
  create: (boardId, { content, parentId }) =>
    unwrap(() => apiClient.post(`/api/boards/${boardId}/comments`, {
      content,
      ...(parentId === undefined ? {} : { parentId }),
    })),
};

export default commentsApi;
