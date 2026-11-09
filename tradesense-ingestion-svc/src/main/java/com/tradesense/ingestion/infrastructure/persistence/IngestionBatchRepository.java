package com.tradesense.ingestion.infrastructure.persistence;

import com.tradesense.common.model.entity.IngestionBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IngestionBatchRepository extends JpaRepository<IngestionBatch, UUID> {
    boolean existsByFileChecksum(String fileChecksum);
}
