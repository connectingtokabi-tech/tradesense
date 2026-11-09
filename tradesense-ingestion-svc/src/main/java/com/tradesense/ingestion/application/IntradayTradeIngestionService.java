package com.tradesense.ingestion.application;

import com.tradesense.common.model.dto.IntradayTradeRequest;
import com.tradesense.common.model.dto.IntradayTradeResponse;
import com.tradesense.common.model.entity.IngestionBatch;
import com.tradesense.common.model.entity.IngestionRecord;
import com.tradesense.common.model.entity.enums.SourceType;
import com.tradesense.ingestion.infrastructure.persistence.IngestionBatchRepository;
import com.tradesense.ingestion.infrastructure.persistence.IngestionRecordRepository;
import com.tradesense.ingestion.infrastructure.tracing.TraceparentHeader;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class IntradayTradeIngestionService {

    private final IngestionBatchRepository batchRepository;
    private final IngestionRecordRepository recordRepository;
    private final Tracer tracer;

    @Transactional
    public IntradayTradeResponse ingest(IntradayTradeRequest request) {
        validate(request);
        if (recordRepository.existsBySourceTypeAndSourceSystemAndSourceEventId(
                SourceType.INTRADAY, request.sourceSystem(), request.sourceEventId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Intraday event has already been received: " + request.sourceEventId());
        }

        String batchName = "INTRADAY-" + request.sourceEventId();
        if (batchName.length() > 150) {
            batchName = batchName.substring(0, 150);
        }
        IngestionBatch batch = new IngestionBatch(
                batchName, SourceType.INTRADAY, null, null, null);
        batch.setTraceparent(TraceparentHeader.fromCurrentSpan(tracer));
        batch.setTotalRecords(1);
        batch = batchRepository.saveAndFlush(batch);

        IngestionRecord record = IngestionRecord.forIntradayMessage(
                batch, request.sourceSystem(), request.sourceEventId(), request.tradeInfo());
        try {
            record = recordRepository.saveAndFlush(record);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Intraday event has already been received: " + request.sourceEventId(), exception);
        }

        return IntradayTradeResponse.builder()
                .batchId(batch.getBatchId())
                .recordId(record.getRecordId())
                .sourceEventId(record.getSourceEventId())
                .status(record.getIngestionStatus().name())
                .message("Intraday trade stored")
                .build();
    }

    private void validate(IntradayTradeRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.sourceSystem() == null || request.sourceSystem().isBlank()
                || request.sourceSystem().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Source system must be between 1 and 100 characters");
        }
        if (request.sourceEventId() == null || request.sourceEventId().isBlank()
                || request.sourceEventId().length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Source event ID must be between 1 and 150 characters");
        }
        if (request.tradeInfo() == null || !request.tradeInfo().isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Trade info must be a JSON object");
        }
    }
}
