/**
 * DecisionHub API Service — Domain Operations
 *
 * All requests use the shared Axios instance from api/axios.js which:
 *   - Sets baseURL from VITE_API_URL (env var)
 *   - Attaches the JWT Bearer token from localStorage on every request
 *
 * Error handling: callers receive the raw Axios error so they can display
 * the backend message (error.response?.data?.message) in the UI.
 */

import api from "./axios";

// ── Communities ──────────────────────────────────────────────────────────────

export const communityApi = {
  create: (communityName, description) =>
    api.post("/api/communities", { communityName, description }),

  getAll: () =>
    api.get("/api/communities"),

  getById: (id) =>
    api.get(`/api/communities/${id}`),
};

// ── Decisions ────────────────────────────────────────────────────────────────

export const decisionApi = {
  create: (payload) =>
    api.post("/api/decisions", payload),

  getAll: () =>
    api.get("/api/decisions"),

  getById: (id) =>
    api.get(`/api/decisions/${id}`),

  update: (id, payload) =>
    api.put(`/api/decisions/${id}`, payload),
};

// ── Options ──────────────────────────────────────────────────────────────────

export const optionApi = {
  add: (decisionId, optionText) =>
    api.post(`/api/decisions/${decisionId}/options`, { optionText }),

  getAll: (decisionId) =>
    api.get(`/api/decisions/${decisionId}/options`),
};

// ── Votes ────────────────────────────────────────────────────────────────────

export const voteApi = {
  cast: (decisionId, payload) =>
    api.post(`/api/decisions/${decisionId}/votes`, payload),
};

// ── Comments ─────────────────────────────────────────────────────────────────

export const commentApi = {
  add: (decisionId, commentText, parentId = null) =>
    api.post(`/api/decisions/${decisionId}/comments`, { commentText, parentId }),

  getAll: (decisionId) =>
    api.get(`/api/decisions/${decisionId}/comments`),
};

// ── Notifications ─────────────────────────────────────────────────────────────

export const notificationApi = {
  getMyNotifications: () =>
    api.get("/api/notifications"),

  markAsRead: (notificationId) =>
    api.patch(`/api/notifications/${notificationId}/read`),
};

// ── Reports ──────────────────────────────────────────────────────────────────

export const reportApi = {
  create: (targetType, targetId, reason) =>
    api.post("/api/reports", { targetType, targetId, reason }),

  // Admin only
  getAll: () =>
    api.get("/api/admin/reports"),

  updateStatus: (reportId, status) =>
    api.patch(`/api/admin/reports/${reportId}`, { status }),
};

// ── Users ─────────────────────────────────────────────────────────────────────

export const userApi = {
  getById: (id) =>
    api.get(`/api/users/${id}`),

  update: (id, fullName) =>
    api.put(`/api/users/${id}`, { fullName }),
};
