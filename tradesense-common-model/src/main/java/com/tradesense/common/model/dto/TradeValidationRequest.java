package com.tradesense.common.model.dto;

import com.tradesense.common.model.entity.Trade;
import com.tradesense.common.model.entity.TradeInstrumentation;

import java.util.UUID;

public record TradeValidationRequest(
        UUID ingestionRecordId,
        Trade trade,
        TradeInstrumentation instrumentation) {
}
