import api from './api'

// Register a new user
export const registerUser = async (userData) => {
  const response = await api.post('/auth/register', userData)
  return response.data
}
