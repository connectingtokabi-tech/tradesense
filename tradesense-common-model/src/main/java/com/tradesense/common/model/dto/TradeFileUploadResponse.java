package com.tradesense.common.model.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record TradeFileUploadResponse(
        UUID batchId,
        String fileName,
        long sizeBytes,
        String checksum,
        String status,
        String message) {
}
