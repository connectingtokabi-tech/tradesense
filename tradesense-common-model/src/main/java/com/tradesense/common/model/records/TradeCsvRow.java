package com.tradesense.common.model.records;

import tools.jackson.databind.JsonNode;

public record TradeCsvRow(long sourceRowNumber, JsonNode rawPayload) {
}

