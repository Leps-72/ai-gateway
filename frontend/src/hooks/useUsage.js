import { useCallback, useEffect, useState } from 'react'
import { getUsage } from '../services/gatewayService.js'

export function getUsageErrorMessage(error) {
  if (error.response?.status === 401) return null
  if (error.response?.status === 429) {
    return error.response?.data?.message || 'Too many requests. Please try again later.'
  }
  return 'Unable to load usage data.'
}

export default function useUsage() {
  const [usage, setUsage] = useState(null)
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState(null)

  const loadUsage = useCallback(async (refresh = false) => {
    if (refresh) setRefreshing(true)
    else setLoading(true)
    setError(null)

    try {
      const response = await getUsage()
      setUsage(response)
    } catch (requestError) {
      setError(getUsageErrorMessage(requestError))
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }, [])

  useEffect(() => {
    loadUsage()
  }, [loadUsage])

  return {
    usage,
    loading,
    refreshing,
    error,
    refresh: () => loadUsage(Boolean(usage)),
  }
}
