const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
).replace(/\/+$/, "");

export class ApiError extends Error {
  constructor(message, status, fieldErrors = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

async function request(path, { body, token } = {}) {
  const headers = { Accept: "application/json" };

  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method: body === undefined ? "GET" : "POST",
      headers,
      ...(body === undefined ? {} : { body: JSON.stringify(body) }),
    });
  } catch (error) {
    throw new Error(
      `Could not connect to the backend at ${API_BASE_URL}. Start the Spring Boot API and try again.`,
      { cause: error },
    );
  }

  const data = await response.json();

  if (!response.ok) {
    const fieldErrors = data?.fieldErrors ?? {};
    const fieldMessage = Object.entries(fieldErrors)
      .map(([field, message]) => `${field}: ${message}`)
      .join(". ");
    throw new ApiError(
      fieldMessage || data?.message || `Request failed (${response.status}).`,
      response.status,
      fieldErrors,
    );
  }

  return data;
}

export function login(credentials) {
  return request("/api/auth/login", { body: credentials });
}

export function register(details) {
  return request("/api/auth/register", { body: details });
}

export function getCurrentUser(token) {
  return request("/api/auth/me", { token });
}

export function requestPasswordReset(email) {
  return request("/api/auth/forgot-password", { body: { email } });
}

export function resetPassword(token, newPassword) {
  return request("/api/auth/reset-password", {
    body: { token, newPassword },
  });
}
