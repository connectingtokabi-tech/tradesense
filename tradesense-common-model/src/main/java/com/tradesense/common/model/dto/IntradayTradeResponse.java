package com.tradesense.common.model.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record IntradayTradeResponse(
        UUID batchId,
        UUID recordId,
        String sourceEventId,
        String status,
        String message) {
}
