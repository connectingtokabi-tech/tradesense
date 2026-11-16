import { useEffect, useState } from 'react'
import TradeReports from './TradeReports'
import AppSidebar from './components/AppSidebar'
import OverviewDashboard from './components/OverviewDashboard'
import type { AppTab, StreamState, Trade } from './types'

const STREAM_URL = import.meta.env.VITE_TRADE_STREAM_URL || '/api/v1/trades/stream'

export default function App() {
  const [trades, setTrades] = useState<Trade[]>([])
  const [streamState, setStreamState] = useState<StreamState>('connecting')
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null)
  const [activeTab, setActiveTab] = useState<AppTab>('overview')

  useEffect(() => {
    let cancelled = false
    let reconnectTimer: number | undefined
    let source: EventSource | null = null

    const handleTradeEvent = (message: MessageEvent<string>) => {
      try {
        const notification = JSON.parse(message.data) as Trade
        setTrades((current) => [notification, ...current.filter((trade) => trade.ingestionRecordId !== notification.ingestionRecordId)])
        setLastUpdated(new Date())
      } catch (error) {
        console.error('Could not read trade stream event', error)
      }
    }

    const connect = () => {
      if (cancelled) return

      source = new EventSource(STREAM_URL)
      source.onopen = () => {
        setStreamState('connected')
        if (reconnectTimer) {
          window.clearTimeout(reconnectTimer)
          reconnectTimer = undefined
        }
      }
      source.onerror = () => {
        if (source?.readyState === EventSource.CLOSED) {
          setStreamState('offline')
          source.close()
          if (!cancelled) {
            reconnectTimer = window.setTimeout(connect, 1500)
          }
          return
        }
        setStreamState('reconnecting')
      }
      source.addEventListener('trade-saved', handleTradeEvent as EventListener)
    }

    connect()

    return () => {
      cancelled = true
      if (reconnectTimer) window.clearTimeout(reconnectTimer)
      source?.close()
    }
  }, [])

  const invalidCount = trades.filter((item) => (item.trade?.tradeStatus ?? item.tradeStatus) === 'INVALID').length

  return <div className="app-shell">
    <AppSidebar activeTab={activeTab} tradeCount={trades.length} invalidCount={invalidCount} onTabChange={setActiveTab} />
    <main className="main-content" id="overview">
      {activeTab === 'reports'
        ? <TradeReports />
        : <OverviewDashboard trades={trades} streamState={streamState} lastUpdated={lastUpdated} />}
    </main>
  </div>
}
