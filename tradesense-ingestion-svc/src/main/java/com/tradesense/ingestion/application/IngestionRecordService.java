package com.tradesense.ingestion.application;

import com.tradesense.common.model.entity.IngestionRecord;
import tools.jackson.databind.JsonNode;

public interface IngestionRecordService {
    IngestionRecord storeIntradayMessage(String sourceSystem, String sourceEventId, JsonNode rawPayload);
}
