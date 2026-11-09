import { useMemo, useState } from 'react'
import { ArrowDownRight, ArrowUpRight, Check, Command, Download, ListFilter, Search, SlidersHorizontal } from 'lucide-react'
import type { Trade } from '../types'

type StatusFilter = 'All trades' | 'Valid' | 'Needs review'

export default function RecentTradesTable({ trades }: { trades: Trade[] }) {
  const [activeFilter, setActiveFilter] = useState<StatusFilter>('All trades')
  const [search, setSearch] = useState('')
  const [showFilters, setShowFilters] = useState(false)
  const validCount = trades.filter((item) => item.trade?.tradeStatus === 'VALID').length
  const invalidCount = trades.filter((item) => item.trade?.tradeStatus === 'INVALID').length

  const visibleTrades = useMemo(() => trades.filter((item) => {
    const trade = item.trade
    const statusMatches = activeFilter === 'All trades'
      || (activeFilter === 'Valid' && trade?.tradeStatus === 'VALID')
      || (activeFilter === 'Needs review' && trade?.tradeStatus === 'INVALID')
    const query = search.trim().toLowerCase()
    const searchMatches = !query || [
      trade?.tradeId?.toString(), trade?.bookingSystem, trade?.sourceType,
      item.instrumentation?.instrumentId, item.ingestionRecordId,
    ].some((value) => value?.toLowerCase().includes(query))
    return statusMatches && searchMatches
  }), [trades, activeFilter, search])

  return <section className="panel trades-panel" id="trades">
    <div className="trades-heading">
      <div><div className="panel-title">Recent trades <span className="table-count">{numberFormat(trades.length)}</span></div><div className="panel-subtitle">A live view of your latest processed records</div></div>
      <div className="table-actions">
        <label className="search-field"><Search size={15} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search trades" /><kbd><Command size={10} /> K</kbd></label>
        <div className="filter-control">
          <button className={`filter-button ${showFilters ? 'selected' : ''}`} onClick={() => setShowFilters((value) => !value)}><SlidersHorizontal size={15} /> Filters{activeFilter !== 'All trades' && <span className="filter-count">1</span>}</button>
          {showFilters && <div className="filter-menu">{(['All trades', 'Valid', 'Needs review'] as StatusFilter[]).map((filter) => <button key={filter} onClick={() => { setActiveFilter(filter); setShowFilters(false) }} className={activeFilter === filter ? 'chosen' : ''}>{filter}{activeFilter === filter && <Check size={14} />}</button>)}</div>}
        </div>
        <button className="table-export" onClick={() => exportTrades(visibleTrades)}><Download size={15} /><span>Export</span></button>
      </div>
    </div>
    <div className="table-scroll">
      <table>
        <thead><tr><th>TRADE</th><th>INSTRUMENT</th><th>BOOKING SYSTEM</th><th>SOURCE</th><th>QUANTITY</th><th>PRICE</th><th>STATUS</th><th>TIME</th><th /></tr></thead>
        <tbody>
          {visibleTrades.slice(0, 8).map((item) => {
            const trade = item.trade
            const timestamp = item.instrumentation?.tradeTimestamp ?? item.savedAt ?? trade?.createdAt
            return <tr key={item.ingestionRecordId}>
              <td><div className="trade-primary">{trade?.tradeId ? `#${trade.tradeId}` : '—'}</div><div className="trade-secondary">{item.ingestionRecordId.slice(0, 8)}…</div></td>
              <td><div className="instrument-cell"><span className={`instrument-icon ${item.instrumentation?.instrumentId?.includes('/') ? 'fx' : ''}`}>{instrumentGlyph(item.instrumentation?.instrumentId)}</span><strong>{item.instrumentation?.instrumentId ?? '—'}</strong></div></td>
              <td><span className="booking-chip">{trade?.bookingSystem ?? 'Unknown'}</span></td>
              <td><span className="source-text">{trade?.sourceType ?? '—'}</span></td>
              <td className="numeric-cell">{item.instrumentation?.quantity == null ? '—' : numberFormat(item.instrumentation.quantity)}</td>
              <td className="numeric-cell price-cell">{item.instrumentation?.tradePrice == null ? '—' : item.instrumentation.tradePrice.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 5 })}</td>
              <td><StatusBadge status={trade?.tradeStatus} /></td>
              <td className="time-cell">{timestamp ? formatTime(timestamp) : '—'}</td>
              <td><button className="row-more" aria-label={`More about trade ${trade?.tradeId ?? ''}`}>···</button></td>
            </tr>
          })}
        </tbody>
      </table>
      {visibleTrades.length === 0 && <div className="empty-state"><div className="empty-icon"><ListFilter size={20} /></div><strong>{trades.length ? 'No trades match these filters' : 'Waiting for your first trade'}</strong><span>{trades.length ? 'Try another search or filter.' : 'Newly saved trades will appear here as they flow through TradeSense.'}</span>{trades.length > 0 && <button onClick={() => { setSearch(''); setActiveFilter('All trades') }}>Clear filters</button>}</div>}
    </div>
    <div className="table-footer"><span>Showing <strong>{visibleTrades.length ? 1 : 0}–{Math.min(8, visibleTrades.length)}</strong> of <strong>{visibleTrades.length}</strong> trades</span><div className="pagination"><button disabled><ArrowDownRight size={14} /></button><span>1</span><button disabled><ArrowUpRight size={14} /></button></div></div>
  </section>
}

function StatusBadge({ status }: { status?: string | null }) {
  if (status === 'VALID') return <span className="status-badge valid"><span />Validated</span>
  if (status === 'INVALID') return <span className="status-badge invalid"><span />Needs review</span>
  return <span className="status-badge pending"><span />Pending</span>
}

function instrumentGlyph(instrument?: string | null) {
  if (!instrument) return '·'
  const parts = instrument.split('/')
  return parts.length > 1 ? `${parts[0].slice(0, 1)}${parts[1].slice(0, 1)}` : instrument.slice(0, 2).toUpperCase()
}

function numberFormat(value: number) { return new Intl.NumberFormat('en-US').format(value) }
function formatTime(value: string) {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
}

function exportTrades(trades: Trade[]) {
  const header = ['tradeId', 'instrument', 'bookingSystem', 'sourceType', 'quantity', 'price', 'status', 'ingestionRecordId']
  const rows = trades.map((item) => [item.trade?.tradeId ?? '', item.instrumentation?.instrumentId ?? '', item.trade?.bookingSystem ?? '', item.trade?.sourceType ?? '', item.instrumentation?.quantity ?? '', item.instrumentation?.tradePrice ?? '', item.trade?.tradeStatus ?? '', item.ingestionRecordId])
  const csv = [header, ...rows].map((row) => row.map((value) => `"${String(value).replaceAll('"', '""')}"`).join(',')).join('\n')
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
  const link = document.createElement('a')
  link.href = url
  link.download = 'tradesense-trades.csv'
  link.click()
  URL.revokeObjectURL(url)
}
