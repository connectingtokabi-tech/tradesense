package com.tradesense.common.model.records;

import tools.jackson.databind.JsonNode;

public record ParsedTradeCsvRow(long sourceRowNumber, String sourceSystem, JsonNode rawPayload) {
}

