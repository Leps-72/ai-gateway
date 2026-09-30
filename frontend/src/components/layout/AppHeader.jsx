import { Menu } from 'lucide-react'
import { useLocation } from 'react-router-dom'

const pageNames = {
  '/dashboard': 'Dashboard',
  '/chat': 'AI Chat',
  '/analyze': 'Analyze',
  '/history': 'History',
  '/usage': 'Usage',
}

function AppHeader({ onMenuClick }) {
  const { pathname } = useLocation()

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
      <div className="api-status" aria-label="API status not checked">
        <span className="status-dot neutral" />
        <div>
          <span>API Status</span>
          <strong>Not checked</strong>
        </div>
      </div>
    </header>
  )
}

export default AppHeader
