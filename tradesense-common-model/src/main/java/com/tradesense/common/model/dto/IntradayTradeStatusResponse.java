package com.tradesense.common.model.dto;

import com.tradesense.common.model.entity.enums.IngestionBatchStatus;
import com.tradesense.common.model.entity.enums.IngestionRecordStatus;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record IntradayTradeStatusResponse(
        UUID batchId,
        UUID recordId,
        String sourceEventId,
        IngestionBatchStatus batchStatus,
        IngestionRecordStatus recordStatus,
        long totalRecords,
        long processedRecords,
        OffsetDateTime receivedAt,
        OffsetDateTime completedAt) {
}
