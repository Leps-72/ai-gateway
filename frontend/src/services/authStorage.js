export const AUTH_TOKEN_KEY = 'ai_gateway_token'
export const AUTH_UNAUTHORIZED_EVENT = 'ai-gateway:unauthorized'

export function getStoredToken() {
  return localStorage.getItem(AUTH_TOKEN_KEY)
}

export function storeToken(token) {
  localStorage.setItem(AUTH_TOKEN_KEY, token)
}

export function clearStoredToken() {
  localStorage.removeItem(AUTH_TOKEN_KEY)
}
