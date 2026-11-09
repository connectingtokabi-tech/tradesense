package com.tradesense.ingestion.application;

import com.tradesense.common.model.dto.FileStatusResponse;

import java.util.UUID;

public interface FileStatusService {
    FileStatusResponse getStatus(UUID batchId);
}
