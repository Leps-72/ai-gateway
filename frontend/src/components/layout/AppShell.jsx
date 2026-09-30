import { useEffect, useState } from 'react'
import { Outlet } from 'react-router-dom'
import api from '../../services/api'
import AppHeader from './AppHeader'
import Sidebar from './Sidebar'

function AppShell() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [apiStatus, setApiStatus] = useState('checking')

  useEffect(() => {
    let active = true
    api.get('/health')
      .then(() => {
        if (active) setApiStatus('online')
      })
      .catch(() => {
        if (active) setApiStatus('offline')
      })

    return () => {
      active = false
    }
  }, [])

  return (
    <div className="app-shell">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      {sidebarOpen && (
        <button
          className="sidebar-overlay"
          type="button"
          aria-label="Close navigation"
          onClick={() => setSidebarOpen(false)}
        />
      )}
      <div className="app-main">
        <AppHeader onMenuClick={() => setSidebarOpen(true)} apiStatus={apiStatus} />
        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default AppShell
