import { useEffect, useState } from 'react'
import { ChevronLeft, ChevronRight, RefreshCw, Search } from 'lucide-react'

type ReportTrade = {
  id: string
  ingestionRecordId: string
  bookingSystem: string | null
  tradeId: number | null
  sourceType: string | null
  tradeStatus: 'VALID' | 'INVALID'
  createdAt: string
  updatedAt: string
}

type InstrumentationData = {
  id: string
  instrumentId: string | null
  quantity: number | null
  tradePrice: number | null
  tradeTimestamp: string | null
}

type TradePage = {
  content?: ReportTrade[]
  number?: number
  size?: number
  totalElements?: number
  totalPages?: number
  page?: {
    number?: number
    size?: number
    totalElements?: number
    totalPages?: number
  }
}

type StatusFilter = '' | 'VALID' | 'INVALID'

export default function TradeReports() {
  const [page, setPage] = useState<TradePage | null>(null)
  const [status, setStatus] = useState<StatusFilter>('')
  const [bookingSystem, setBookingSystem] = useState('')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [pageNumber, setPageNumber] = useState(0)
  const [refreshKey, setRefreshKey] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [selectedTradeId, setSelectedTradeId] = useState<string | null>(null)
  const [instrumentation, setInstrumentation] = useState<InstrumentationData | null>(null)
  const [instrumentationLoading, setInstrumentationLoading] = useState(false)

  useEffect(() => {
    const controller = new AbortController()
    const params = new URLSearchParams({ page: String(pageNumber), size: '25', sort: 'createdAt,desc' })
    if (status) params.set('status', status)
    if (bookingSystem.trim()) params.set('bookingSystem', bookingSystem.trim())
    if (from) params.set('from', `${from}T00:00:00Z`)
    if (to) {
      const endDate = new Date(`${to}T00:00:00Z`)
      endDate.setUTCDate(endDate.getUTCDate() + 1)
      params.set('to', endDate.toISOString())
    }

    setLoading(true)
    setError('')
    fetch(`/api/reports/trades?${params}`, { signal: controller.signal })
      .then(async (response) => {
        if (!response.ok) throw new Error(`Report request failed (${response.status})`)
        return response.json() as Promise<TradePage>
      })
      .then(setPage)
      .catch((cause: unknown) => {
        if (cause instanceof Error && cause.name !== 'AbortError') setError(cause.message)
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })

    return () => controller.abort()
  }, [pageNumber, status, bookingSystem, from, to, refreshKey])

  useEffect(() => {
    if (!selectedTradeId) {
      setInstrumentation(null)
      return
    }

    const controller = new AbortController()
    setInstrumentationLoading(true)
    fetch(`/api/reports/trades/${selectedTradeId}/instrumentation`, { signal: controller.signal })
      .then(async (response) => {
        if (!response.ok) throw new Error(`Instrumentation request failed (${response.status})`)
        return response.json() as Promise<InstrumentationData>
      })
      .then(setInstrumentation)
      .catch((cause: unknown) => {
        if (!(cause instanceof Error && cause.name === 'AbortError')) {
          setInstrumentation(null)
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setInstrumentationLoading(false)
      })

    return () => controller.abort()
  }, [selectedTradeId])

  const changeFilter = (update: () => void) => {
    setPageNumber(0)
    update()
  }

  const handleTradeSelect = (tradeId: string) => {
    setSelectedTradeId((current) => current === tradeId ? null : tradeId)
  }

  const pageMeta = page ? {
    content: page.content ?? [],
    number: page.number ?? page.page?.number ?? 0,
    size: page.size ?? page.page?.size ?? 25,
    totalElements: page.totalElements ?? page.page?.totalElements ?? 0,
    totalPages: page.totalPages ?? page.page?.totalPages ?? 1,
  } : null

  return (
    <>
      <section className="page-heading report-heading">
        <div>
          <div className="eyebrow"><span className="eyebrow-line" /> REPORTING</div>
          <h1>Trade reports</h1>
          <p className="page-subtitle">Search and review processed trade records.</p>
        </div>
        <button className="export-button" onClick={() => setRefreshKey((key) => key + 1)} disabled={loading}>
          <RefreshCw size={15} /> Refresh
        </button>
      </section>

      <section className="panel report-panel">
        <div className="report-filters">
          <label className="report-filter-label">Status
            <select value={status} onChange={(event) => changeFilter(() => setStatus(event.target.value as StatusFilter))}>
              <option value="">All statuses</option>
              <option value="VALID">Valid</option>
              <option value="INVALID">Needs review</option>
            </select>
          </label>
          <label className="report-filter-label">Booking system
            <span className="report-search"><Search size={14} /><input value={bookingSystem} onChange={(event) => changeFilter(() => setBookingSystem(event.target.value))} placeholder="Any booking system" /></span>
          </label>
          <label className="report-filter-label">Created from
            <input type="date" value={from} onChange={(event) => changeFilter(() => setFrom(event.target.value))} />
          </label>
          <label className="report-filter-label">Created through
            <input type="date" value={to} onChange={(event) => changeFilter(() => setTo(event.target.value))} />
          </label>
        </div>

        {error ? <div className="report-message error-message">{error}</div> : (
          <>
            <div className="report-table-scroll">
              <table>
                <thead><tr><th>TRADE ID</th><th>BOOKING SYSTEM</th><th>SOURCE</th><th>STATUS</th><th>CREATED</th><th>INGESTION RECORD</th></tr></thead>
                <tbody>
                  {pageMeta?.content.map((trade) => <tr key={trade.id} onClick={() => handleTradeSelect(trade.id)} className={selectedTradeId === trade.id ? 'selected' : ''}>
                    <td>
                      <button
                        type="button"
                        className="trade-primary trade-select-button"
                        onClick={(event) => {
                          event.stopPropagation()
                          handleTradeSelect(trade.id)
                        }}
                      >
                        {trade.tradeId == null ? '—' : `#${trade.tradeId}`}
                      </button>
                    </td>
                    <td>{trade.bookingSystem ?? '—'}</td>
                    <td>{trade.sourceType ?? '—'}</td>
                    <td><span className={`status-badge ${trade.tradeStatus === 'VALID' ? 'valid' : 'invalid'}`}><span />{trade.tradeStatus === 'VALID' ? 'Validated' : 'Needs review'}</span></td>
                    <td>{formatReportDate(trade.createdAt)}</td>
                    <td><span className="trade-secondary">{trade.ingestionRecordId}</span></td>
                  </tr>)}
                </tbody>
              </table>
              {!loading && pageMeta && pageMeta.content.length === 0 && <div className="empty-state"><strong>No trades found</strong><span>Change the filters and try again.</span></div>}
              {loading && <div className="report-message">Loading report…</div>}
            </div>
            <div className="table-footer">
              <span>{pageMeta ? `${pageMeta.totalElements.toLocaleString()} trades` : 'Loading…'}</span>
              <div className="report-pagination">
                <button aria-label="Previous page" disabled={!pageMeta || pageMeta.number <= 0 || loading} onClick={() => setPageNumber((number) => number - 1)}><ChevronLeft size={15} /></button>
                <span>{pageMeta ? `${pageMeta.number + 1} / ${Math.max(1, pageMeta.totalPages)}` : '—'}</span>
                <button aria-label="Next page" disabled={!pageMeta || pageMeta.number + 1 >= pageMeta.totalPages || loading} onClick={() => setPageNumber((number) => number + 1)}><ChevronRight size={15} /></button>
              </div>
            </div>
            {selectedTradeId && (
              <div
                style={{
                  position: 'fixed',
                  inset: 0,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  background: 'rgba(15, 23, 42, 0.38)',
                  backdropFilter: 'blur(4px)',
                  zIndex: 100,
                  padding: '24px',
                }}
                onClick={() => setSelectedTradeId(null)}
              >
                <div
                  style={{
                    width: 'min(480px, 92vw)',
                    background: 'rgba(15, 23, 42, 0.68)',
                    border: '1px solid rgba(148, 163, 184, 0.35)',
                    boxShadow: '0 24px 80px rgba(15, 23, 42, 0.45)',
                    borderRadius: '18px',
                    padding: '24px 24px 18px',
                    color: '#e2e8f0',
                    backdropFilter: 'blur(10px)',
                  }}
                  onClick={(event) => event.stopPropagation()}
                >
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '18px' }}>
                    <h2 style={{ margin: 0, fontSize: '1.2rem' }}>Instrumentation Details</h2>
                    <button
                      type="button"
                      aria-label="Close instrumentation details"
                      onClick={() => setSelectedTradeId(null)}
                      style={{
                        border: '1px solid rgba(148, 163, 184, 0.3)',
                        background: 'rgba(15, 23, 42, 0.4)',
                        color: '#f8fafc',
                        borderRadius: '999px',
                        width: '32px',
                        height: '32px',
                        cursor: 'pointer',
                      }}
                    >
                      ×
                    </button>
                  </div>

                  {instrumentationLoading ? (
                    <div style={{ color: '#cbd5e1' }}>Loading instrumentation details…</div>
                  ) : instrumentation ? (
                    <div style={{ display: 'grid', gap: '12px' }}>
                      <div style={{ display: 'grid', gridTemplateColumns: '140px 1fr', gap: '8px 16px' }}>
                        <strong>Instrument ID</strong>
                        <span>{instrumentation.instrumentId ?? '—'}</span>

                        <strong>Quantity</strong>
                        <span>{instrumentation.quantity?.toLocaleString() ?? '—'}</span>

                        <strong>Trade Price</strong>
                        <span>{instrumentation.tradePrice == null ? '—' : new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(instrumentation.tradePrice)}</span>

                        <strong>Trade Timestamp</strong>
                        <span>{instrumentation.tradeTimestamp ?? '—'}</span>
                      </div>
                    </div>
                  ) : (
                    <div style={{ color: '#cbd5e1' }}>No instrumentation found for this trade.</div>
                  )}
                </div>
              </div>
            )}
          </>
        )}
      </section>
    </>
  )
}

function formatReportDate(value: string) {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString()
}
