package com.tradesense.ingestion.adapters.batch;

import com.tradesense.common.model.entity.IngestionBatch;
import com.tradesense.common.model.entity.IngestionRecord;
import com.tradesense.common.model.records.ParsedTradeCsvRow;
import com.tradesense.ingestion.infrastructure.persistence.IngestionBatchRepository;
import com.tradesense.ingestion.infrastructure.persistence.IngestionRecordRepository;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IngestionRecordItemWriter implements ItemWriter<ParsedTradeCsvRow> {

    private final UUID batchId;
    private final String sourceSystem;
    private final IngestionBatchRepository batchRepository;
    private final IngestionRecordRepository recordRepository;

    public IngestionRecordItemWriter(
            UUID batchId,
            String sourceSystem,
            IngestionBatchRepository batchRepository,
            IngestionRecordRepository recordRepository) {
        this.batchId = batchId;
        this.sourceSystem = sourceSystem;
        this.batchRepository = batchRepository;
        this.recordRepository = recordRepository;
    }

    @Override
    public void write(Chunk<? extends ParsedTradeCsvRow> chunk) {
        if (chunk.isEmpty()) {
            return;
        }
        IngestionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No ingestion batch found for " + batchId));

        List<IngestionRecord> records = new ArrayList<>(chunk.size());
        for (ParsedTradeCsvRow row : chunk) {
            String rowSourceSystem = row.sourceSystem() == null ? sourceSystem : row.sourceSystem();
            records.add(IngestionRecord.forEodFileRow(
                    batch, rowSourceSystem, row.sourceRowNumber(), row.rawPayload()));
        }

        recordRepository.saveAll(records);
        batch.setTotalRecords(batch.getTotalRecords() + records.size());
    }
}
