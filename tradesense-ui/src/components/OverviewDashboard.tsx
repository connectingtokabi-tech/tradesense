import { useMemo } from 'react'
import type { CSSProperties, ReactNode } from 'react'
import {
  Bell, BriefcaseBusiness, Check, ChevronDown, CircleHelp, Clock3,
  Download, ShieldAlert, WalletCards,
} from 'lucide-react'
import RecentTradesTable from './RecentTradesTable'
import type { StreamState, Trade } from '../types'

type Props = { trades: Trade[]; streamState: StreamState; lastUpdated: Date | null }

export default function OverviewDashboard({ trades, streamState, lastUpdated }: Props) {
  const normalized = useMemo(() => trades.map((item) => ({
    ...item,
    trade: item.trade ?? {
      id: item.id,
      ingestionRecordId: item.ingestionRecordId,
      bookingSystem: item.bookingSystem,
      tradeId: item.tradeId,
      sourceType: item.sourceType,
      tradeStatus: item.tradeStatus,
      createdAt: item.createdAt,
    },
  })), [trades])

  const validCount = normalized.filter((item) => item.trade?.tradeStatus === 'VALID').length
  const invalidCount = normalized.filter((item) => item.trade?.tradeStatus === 'INVALID').length
  const totalQuantity = normalized.reduce((total, item) => total + (item.instrumentation?.quantity ?? 0), 0)
  const chartPoints = useMemo(() => {
    const today = new Date()
    const points = Array.from({ length: 7 }, (_, index) => {
      const date = new Date(today)
      date.setDate(today.getDate() - (6 - index))
      return { date: date.toLocaleDateString('en-US', { weekday: 'short' }), count: 0, day: date.toDateString() }
    })
    normalized.forEach((item) => {
      const timestamp = item.instrumentation?.tradeTimestamp ?? item.trade?.createdAt ?? item.savedAt
      if (!timestamp) return
      const point = points.find((entry) => entry.day === new Date(timestamp).toDateString())
      if (point) point.count += 1
    })
    return points
  }, [normalized])

  const chartMax = Math.max(4, ...chartPoints.map((point) => point.count))
  const chartPolyline = chartPoints.map((point, index) => `${index * 100},${92 - (point.count / chartMax) * 70}`).join(' ')
  const chartArea = `0,100 ${chartPolyline} 600,100`

  return <>
    <header className="topbar">
      <div className="breadcrumb"><span>Workspace</span><span className="crumb-slash">/</span><strong>Overview</strong></div>
      <div className="topbar-actions">
        <div className={`connection-pill ${streamState}`}><span className="live-dot" /><span>{streamState === 'connected' ? 'Live stream connected' : streamState === 'connecting' ? 'Connecting to stream' : streamState === 'offline' ? 'Stream offline' : 'Reconnecting'}</span></div>
        <button className="icon-button notification-button" aria-label="Notifications"><Bell size={17} /><i /></button>
        <div className="topbar-divider" />
        <button className="help-button"><CircleHelp size={15} /> Help</button>
      </div>
    </header>

    <section className="page-heading">
      <div>
        <div className="eyebrow"><span className="eyebrow-line" /> TRADE MONITORING</div>
        <h1>Good morning <span className="wave">✳</span></h1>
        <p className="page-subtitle">Here’s what’s happening across your trade operations today.</p>
      </div>
      <div className="heading-meta">
        <div className="date-chip"><Clock3 size={15} /><span>{new Date().toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}</span><ChevronDown size={13} /></div>
        <button className="export-button" onClick={() => exportTrades(normalized)}><Download size={15} /> Export report</button>
      </div>
    </section>

    <section className="metric-grid" aria-label="Trade metrics">
      <MetricCard label="Total trades" value={numberFormat(normalized.length)} note="Records in stream" icon={<BriefcaseBusiness size={17} />} accent="blue" trend="Live" />
      <MetricCard label="Validated" value={numberFormat(validCount)} note={normalized.length ? `${Math.round(validCount / normalized.length * 100)}% pass rate` : 'Awaiting first records'} icon={<Check size={18} />} accent="green" trend="VALID" />
      <MetricCard label="Needs review" value={numberFormat(invalidCount)} note="Flagged by rule engine" icon={<ShieldAlert size={17} />} accent="amber" trend={invalidCount ? 'Attention' : 'Clear'} />
      <MetricCard label="Total quantity" value={compactNumber(totalQuantity)} note="Across processed trades" icon={<WalletCards size={17} />} accent="violet" trend="Lots" />
    </section>

    <section className="insight-grid">
      <article className="panel activity-panel">
        <div className="panel-heading">
          <div><div className="panel-title">Trade activity</div><div className="panel-subtitle">Processed trades over the last 7 days</div></div>
          <button className="select-button">Last 7 days <ChevronDown size={14} /></button>
        </div>
        <div className="chart-legend"><span className="legend-dot" /> Trades processed <span className="chart-total">{numberFormat(normalized.length)} total</span></div>
        <div className="chart-wrap">
          <div className="chart-y-labels"><span>{chartMax}</span><span>{Math.ceil(chartMax * .66)}</span><span>{Math.ceil(chartMax * .33)}</span><span>0</span></div>
          <svg className="activity-chart" viewBox="0 0 600 110" preserveAspectRatio="none" role="img" aria-label="Trade activity for the last seven days">
            <defs><linearGradient id="areaFill" x1="0" x2="0" y1="0" y2="1"><stop offset="0%" stopColor="#5278f6" stopOpacity=".2" /><stop offset="100%" stopColor="#5278f6" stopOpacity="0" /></linearGradient></defs>
            {[22, 45, 68, 92].map((y) => <line key={y} x1="0" x2="600" y1={y} y2={y} className="grid-line" />)}
            {normalized.length > 0 && <><polygon points={chartArea} fill="url(#areaFill)" /><polyline points={chartPolyline} className="chart-line" />{chartPoints.map((point, index) => <circle key={point.day} cx={index * 100} cy={92 - (point.count / chartMax) * 70} r="3.5" className="chart-point" />)}</>}
          </svg>
        </div>
        <div className="chart-x-labels">{chartPoints.map((point) => <span key={point.day}>{point.date}</span>)}</div>
      </article>

      <article className="panel validation-panel" id="validations">
        <div className="panel-heading">
          <div><div className="panel-title">Validation health</div><div className="panel-subtitle">Rule engine outcomes</div></div>
          <button className="more-button" aria-label="More options">···</button>
        </div>
        <div className="validation-body">
          <div className="donut-wrap" style={{ '--valid-ratio': `${normalized.length ? validCount / normalized.length * 100 : 0}%` } as CSSProperties}>
            <div className="donut-center"><strong>{normalized.length ? `${Math.round(validCount / normalized.length * 100)}%` : '—'}</strong><span>pass rate</span></div>
          </div>
          <div className="validation-legend">
            <div className="validation-legend-row"><span className="legend-marker valid-marker" /><span>Passed</span><strong>{numberFormat(validCount)}</strong></div>
            <div className="validation-legend-row"><span className="legend-marker invalid-marker" /><span>Needs review</span><strong>{numberFormat(invalidCount)}</strong></div>
          </div>
        </div>
        <div className="rule-status"><span className="rule-check"><Check size={12} /></span><span>Drools rules are operational</span><span className="rule-live">LIVE</span></div>
      </article>
    </section>

    <RecentTradesTable trades={normalized} />
    <footer className="page-footer"><span><span className="footer-mark">✳</span> TradeSense Operations</span><span>{lastUpdated ? `Last event received ${lastUpdated.toLocaleTimeString()}` : 'Connected to live trade processing'}</span><span className="footer-version">v1.0.0</span></footer>
  </>
}

function MetricCard({ label, value, note, icon, accent, trend }: { label: string; value: string; note: string; icon: ReactNode; accent: string; trend: string }) {
  return <article className={`metric-card ${accent}`}><div className="metric-top"><span className="metric-icon">{icon}</span><span className={`metric-trend ${accent}`}>{trend}</span></div><div className="metric-value">{value}</div><div className="metric-label">{label}</div><div className="metric-note">{note}</div></article>
}

function numberFormat(value: number) { return new Intl.NumberFormat('en-US').format(value) }
function compactNumber(value: number) { return new Intl.NumberFormat('en-US', { notation: 'compact', maximumFractionDigits: 1 }).format(value) }

function exportTrades(trades: Trade[]) {
  const header = ['tradeId', 'instrument', 'bookingSystem', 'sourceType', 'quantity', 'price', 'status', 'ingestionRecordId']
  const rows = trades.map((item) => [
    item.trade?.tradeId ?? '', item.instrumentation?.instrumentId ?? '', item.trade?.bookingSystem ?? '',
    item.trade?.sourceType ?? '', item.instrumentation?.quantity ?? '', item.instrumentation?.tradePrice ?? '',
    item.trade?.tradeStatus ?? '', item.ingestionRecordId,
  ])
  const csv = [header, ...rows].map((row) => row.map((value) => `"${String(value).replaceAll('"', '""')}"`).join(',')).join('\n')
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
  const link = document.createElement('a')
  link.href = url
  link.download = 'tradesense-trades.csv'
  link.click()
  URL.revokeObjectURL(url)
}
