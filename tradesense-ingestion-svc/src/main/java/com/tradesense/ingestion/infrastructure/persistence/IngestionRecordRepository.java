package com.tradesense.ingestion.infrastructure.persistence;

import com.tradesense.common.model.entity.IngestionRecord;
import com.tradesense.common.model.entity.enums.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IngestionRecordRepository extends JpaRepository<IngestionRecord, UUID> {
    long countByBatch_BatchId(UUID batchId);

    boolean existsBySourceTypeAndSourceSystemAndSourceEventId(
            SourceType sourceType, String sourceSystem, String sourceEventId);

    Optional<IngestionRecord> findByBatch_BatchId(UUID batchId);
}
