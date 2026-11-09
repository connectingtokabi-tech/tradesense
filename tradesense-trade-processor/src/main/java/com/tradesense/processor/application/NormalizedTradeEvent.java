package com.tradesense.processor.application;

import java.util.UUID;

public record NormalizedTradeEvent(UUID ingestionRecordId, TradePayload tradePayload) {
}
