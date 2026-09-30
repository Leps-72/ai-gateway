import api from './api.js'

export async function getUsage() {
  const response = await api.get('/usage')
  return response.data
}
