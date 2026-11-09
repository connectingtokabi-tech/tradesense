package com.tradesense.processor.application;

import com.tradesense.common.model.entity.enums.SourceType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Normalized attributes extracted from a CDC event before entity mapping. */
public record TradePayload(
        String bookingSystem,
        Long tradeId,
        SourceType sourceType,
        String instrumentId,
        Long quantity,
        BigDecimal tradePrice,
        OffsetDateTime tradeTimestamp) {
}
