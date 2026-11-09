package com.tradesense.common.model.dto;

import lombok.Builder;
import tools.jackson.databind.JsonNode;

@Builder
public record IntradayTradeRequest(
        String sourceSystem,
        String sourceEventId,
        JsonNode tradeInfo) {
}
