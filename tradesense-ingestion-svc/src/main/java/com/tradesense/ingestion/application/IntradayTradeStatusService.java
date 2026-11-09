package com.tradesense.ingestion.application;

import com.tradesense.common.model.dto.IntradayTradeStatusResponse;
import com.tradesense.common.model.entity.IngestionBatch;
import com.tradesense.common.model.entity.IngestionRecord;
import com.tradesense.common.model.entity.enums.SourceType;
import com.tradesense.ingestion.infrastructure.persistence.IngestionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntradayTradeStatusService {

    private final IngestionRecordRepository recordRepository;

    @Transactional(readOnly = true)
    public IntradayTradeStatusResponse getStatus(UUID batchId) {
        IngestionRecord record = recordRepository.findByBatch_BatchId(batchId)
                .filter(candidate -> candidate.getSourceType() == SourceType.INTRADAY)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No intraday ingestion found for batch " + batchId));
        IngestionBatch batch = record.getBatch();

        return IntradayTradeStatusResponse.builder()
                .batchId(batch.getBatchId())
                .recordId(record.getRecordId())
                .sourceEventId(record.getSourceEventId())
                .batchStatus(batch.getStatus())
                .recordStatus(record.getIngestionStatus())
                .totalRecords(batch.getTotalRecords())
                .processedRecords(batch.getProcessedRecords())
                .receivedAt(record.getReceivedAt())
                .completedAt(batch.getCompletedAt())
                .build();
    }
}
