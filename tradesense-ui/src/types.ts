export type Trade = {
  id?: string
  ingestionRecordId: string
  bookingSystem?: string | null
  tradeId?: number | null
  sourceType?: string | null
  tradeStatus?: 'VALID' | 'INVALID' | string | null
  createdAt?: string | null
  trade?: {
    id?: string
    ingestionRecordId?: string
    bookingSystem?: string | null
    tradeId?: number | null
    sourceType?: string | null
    tradeStatus?: 'VALID' | 'INVALID' | string | null
    createdAt?: string | null
  }
  instrumentation?: {
    instrumentId?: string | null
    quantity?: number | null
    tradePrice?: number | null
    tradeTimestamp?: string | null
  }
  savedAt?: string
}

export type StreamState = 'connecting' | 'connected' | 'reconnecting' | 'offline'
export type AppTab = 'overview' | 'reports'
