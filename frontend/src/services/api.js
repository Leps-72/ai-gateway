import axios from 'axios'
import {
  AUTH_UNAUTHORIZED_EVENT,
  clearStoredToken,
  getStoredToken,
} from './authStorage.js'

const api = axios.create({
  baseURL: import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

const publicPaths = ['/auth/login', '/auth/register', '/health']

function isPublicRequest(url = '') {
  return publicPaths.some((path) => url.endsWith(path))
}

api.interceptors.request.use((config) => {
  if (!isPublicRequest(config.url)) {
    const token = getStoredToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isUnauthorized = error.response?.status === 401
    const requestUrl = error.config?.url || ''

    if (isUnauthorized && !isPublicRequest(requestUrl)) {
      clearStoredToken()
      window.dispatchEvent(new Event(AUTH_UNAUTHORIZED_EVENT))
    }

    return Promise.reject(error)
  },
)

export default api
