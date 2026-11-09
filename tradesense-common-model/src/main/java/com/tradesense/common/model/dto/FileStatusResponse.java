package com.tradesense.common.model.dto;

import com.tradesense.common.model.entity.enums.IngestionBatchStatus;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record FileStatusResponse(
        UUID batchId,
        String fileName,
        IngestionBatchStatus status,
        long totalRecords,
        long processedRecords,
        long validRecords,
        long invalidRecords,
        OffsetDateTime uploadedAt,
        OffsetDateTime completedAt) {
}
