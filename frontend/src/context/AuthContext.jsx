
import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {

  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)

  // Store authenticated user information
  const login = (userData, accessToken) => {
    setUser(userData)
    setToken(accessToken)
  }

  // Clear authentication information
  const logout = () => {
    setUser(null)
    setToken(null)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: Boolean(token),
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

// Custom hook to access authentication context
export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }

  return context
}
