import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { login as loginRequest, register as registerRequest } from '../services/authService'
import {
  AUTH_UNAUTHORIZED_EVENT,
  clearStoredToken,
  getStoredToken,
  storeToken,
} from '../services/authStorage'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => getStoredToken())

  useEffect(() => {
    const handleUnauthorized = () => setToken(null)
    window.addEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized)
    return () => window.removeEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized)
  }, [])

  const login = async (username, password) => {
    const response = await loginRequest(username, password)
    if (!response?.token) {
      throw new Error('Authentication response did not include a token.')
    }
    storeToken(response.token)
    setToken(response.token)
  }

  const register = (username, password) => registerRequest(username, password)

  const logout = () => {
    clearStoredToken()
    setToken(null)
  }

  const value = useMemo(() => ({
    token,
    isAuthenticated: Boolean(token),
    login,
    register,
    logout,
  }), [token])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider.')
  }
  return context
}
