import {
  BarChart3,
  Bot,
  Boxes,
  FileClock,
  LayoutDashboard,
  LogOut,
  MessageSquareText,
  ScanSearch,
  X,
} from 'lucide-react'
import { NavLink } from 'react-router-dom'

const navigation = [
  {
    label: 'Overview',
    items: [{ to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard }],
  },
  {
    label: 'Playground',
    items: [
      { to: '/chat', label: 'AI Chat', icon: MessageSquareText },
      { to: '/analyze', label: 'Analyze', icon: ScanSearch },
    ],
  },
  {
    label: 'Monitoring',
    items: [
      { to: '/history', label: 'History', icon: FileClock },
      { to: '/usage', label: 'Usage', icon: BarChart3 },
    ],
  },
]

function Sidebar({ open, onClose }) {
  return (
    <aside className={`sidebar ${open ? 'sidebar-open' : ''}`} aria-label="Primary navigation">
      <div className="brand-row">
        <div className="brand-mark" aria-hidden="true"><Boxes size={20} /></div>
        <div>
          <strong>AI Gateway</strong>
          <span>Management Console</span>
        </div>
        <button className="icon-button sidebar-close" type="button" onClick={onClose} aria-label="Close navigation">
          <X size={20} />
        </button>
      </div>

      <nav className="sidebar-nav">
        {navigation.map((section) => (
          <div className="nav-section" key={section.label}>
            <p>{section.label}</p>
            {section.items.map(({ to, label, icon: Icon }) => (
              <NavLink
                to={to}
                key={to}
                onClick={onClose}
                className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              >
                <Icon size={18} aria-hidden="true" />
                <span>{label}</span>
              </NavLink>
            ))}
          </div>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="environment-chip"><Bot size={15} /> Gemini provider</div>
        <button className="logout-button" type="button" disabled title="Available after authentication integration">
          <LogOut size={18} />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  )
}

export default Sidebar
