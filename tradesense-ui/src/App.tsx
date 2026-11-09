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
    const source = new EventSource(STREAM_URL)
    setStreamState('connecting')
    source.onopen = () => setStreamState('connected')
    source.onerror = () => setStreamState(source.readyState === EventSource.CLOSED ? 'offline' : 'reconnecting')
    source.addEventListener('trade-saved', (message) => {
      try {
        const notification = JSON.parse((message as MessageEvent<string>).data) as Trade
        setTrades((current) => [notification, ...current.filter((trade) => trade.ingestionRecordId !== notification.ingestionRecordId)])
        setLastUpdated(new Date())
      } catch (error) {
        console.error('Could not read trade stream event', error)
      }
    })
    return () => source.close()
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
