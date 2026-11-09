package com.tradesense.common.model.records;

import com.tradesense.common.model.entity.IngestionBatch;

public record UploadResult(IngestionBatch batch, long sizeBytes, String checksum) {
}

