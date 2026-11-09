import { BriefcaseBusiness, ChevronDown, LayoutDashboard, Radio, ShieldAlert } from 'lucide-react'
import type { AppTab } from '../types'

type Props = {
  activeTab: AppTab
  tradeCount: number
  invalidCount: number
  onTabChange: (tab: AppTab) => void
}

export default function AppSidebar({ activeTab, tradeCount, invalidCount, onTabChange }: Props) {
  return (
    <aside className="sidebar">
      <div className="brand-lockup">
        <div className="brand-mark"><span /><span /><span /></div>
        <span className="brand-name">trade<span>sense</span></span>
      </div>
      <div className="workspace-switcher">
        <div className="workspace-avatar">TS</div>
        <div className="workspace-copy"><strong>Trade operations</strong><small>Production workspace</small></div>
        <ChevronDown size={15} />
      </div>
      <div className="nav-section-label">WORKSPACE</div>
      <nav className="main-nav">
        <a className={`nav-item ${activeTab === 'overview' ? 'active' : ''}`} href="#overview" onClick={() => onTabChange('overview')}><LayoutDashboard size={17} /><span>Overview</span></a>
        <a className="nav-item" href="#trades" onClick={() => onTabChange('overview')}><BriefcaseBusiness size={17} /><span>Trade activity</span><span className="nav-count">{tradeCount}</span></a>
        <a className="nav-item" href="#validations" onClick={() => onTabChange('overview')}><ShieldAlert size={17} /><span>Validations</span>{invalidCount > 0 && <span className="nav-alert">{invalidCount}</span>}</a>
        <a className="nav-item" href="#stream" onClick={() => onTabChange('overview')}><Radio size={17} /><span>Event stream</span><span className="live-dot small" /></a>
        <button className={`nav-item nav-button ${activeTab === 'reports' ? 'active' : ''}`} onClick={() => onTabChange('reports')}><BriefcaseBusiness size={17} /><span>Reports</span></button>
      </nav>
    </aside>
  )
}
