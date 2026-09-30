import { Menu } from 'lucide-react'
import { useLocation } from 'react-router-dom'

const pageNames = {
  '/dashboard': 'Dashboard',
  '/chat': 'AI Chat',
  '/analyze': 'Analyze',
  '/history': 'History',
  '/usage': 'Usage',
}

const statusContent = {
  checking: { label: 'Checking…', className: 'checking' },
  online: { label: 'API Online', className: 'online' },
  offline: { label: 'API Offline', className: 'offline' },
}

function AppHeader({ onMenuClick, apiStatus }) {
  const { pathname } = useLocation()
  const status = statusContent[apiStatus] || statusContent.checking

  return (
    <header className="app-header">
      <div className="header-title">
        <button className="icon-button mobile-menu" type="button" onClick={onMenuClick} aria-label="Open navigation">
          <Menu size={21} />
        </button>
        <div>
          <span>Workspace</span>
          <strong>{pageNames[pathname] || 'AI Gateway'}</strong>
        </div>
      </div>
      <div className="api-status" aria-live="polite" aria-label={`API status: ${status.label}`}>
        <span className={`status-dot ${status.className}`} />
        <div>
          <span>API Status</span>
          <strong>{status.label}</strong>
        </div>
      </div>
    </header>
  )
}

export default AppHeader
