package com.tradesense.processor.application;

import com.tradesense.common.model.entity.Trade;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TradeSavedNotification(
        UUID ingestionRecordId,
        Trade trade,
        Instrumentation instrumentation,
        OffsetDateTime savedAt) {

    public static TradeSavedNotification from(Trade trade, TradeInstrumentationData data) {
        return new TradeSavedNotification(
                trade.getIngestionRecordId(),
                trade,
                new Instrumentation(data.instrumentId(), data.quantity(), data.tradePrice(), data.tradeTimestamp()),
                trade.getCreatedAt() == null ? OffsetDateTime.now() : trade.getCreatedAt());
    }

    public record TradeInstrumentationData(
            String instrumentId,
            Long quantity,
            BigDecimal tradePrice,
            OffsetDateTime tradeTimestamp) {
    }

    public record Instrumentation(
            String instrumentId,
            Long quantity,
            BigDecimal tradePrice,
            OffsetDateTime tradeTimestamp) {
    }
}
