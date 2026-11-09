package com.tradesense.ingestion.application;

import com.tradesense.common.model.entity.IngestionRecord;
import com.tradesense.ingestion.infrastructure.persistence.IngestionRecordRepository;
import com.tradesense.ingestion.infrastructure.tracing.TraceparentHeader;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class IngestionRecordServiceImpl implements IngestionRecordService {

    private final IngestionRecordRepository recordRepository;
    private final Tracer tracer;

    /** Stores an original MQ message; the event ID is set and the file row number stays null. */
    @Transactional
    @Override
    public IngestionRecord storeIntradayMessage(
            String sourceSystem, String sourceEventId, JsonNode rawPayload) {
        if (sourceEventId == null || sourceEventId.isBlank() || sourceEventId.length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Intraday source event ID must be between 1 and 150 characters");
        }
        if (sourceSystem != null && sourceSystem.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Source system must be at most 100 characters");
        }
        if (rawPayload == null || rawPayload.isNull()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Intraday message payload is required");
        }

        IngestionRecord record = IngestionRecord.forIntradayMessage(sourceSystem, sourceEventId, rawPayload);
        record.setTraceparent(TraceparentHeader.fromCurrentSpan(tracer));
        return recordRepository.save(record);
    }
}

