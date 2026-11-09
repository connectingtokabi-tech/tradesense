package com.tradesense.common.model.entity;

import com.tradesense.common.model.entity.enums.IngestionBatchStatus;
import com.tradesense.common.model.entity.enums.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ingestion_batch")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngestionBatch {

    public IngestionBatch(
            String batchName,
            SourceType sourceType,
            String fileName,
            String fileLocation,
            String fileChecksum) {
        this.batchName = batchName;
        this.sourceType = sourceType;
        this.fileName = fileName;
        this.fileLocation = fileLocation;
        this.fileChecksum = fileChecksum;
        this.totalRecords = 0;
        this.processedRecords = 0;
        this.validRecords = 0;
        this.invalidRecords = 0;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "batch_id", nullable = false, updatable = false)
    private UUID batchId;

    @Column(name = "batch_name", length = 150)
    private String batchName;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20, nullable = false)
    private SourceType sourceType;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_location", length = 500)
    private String fileLocation;

    @Column(name = "file_checksum", length = 128)
    private String fileChecksum;

    @Column(name = "traceparent", length = 55)
    private String traceparent;

    @Column(name = "total_records", nullable = false)
    private long totalRecords;

    @Column(name = "processed_records", nullable = false)
    private long processedRecords;

    @Column(name = "valid_records", nullable = false)
    private long validRecords;

    @Column(name = "invalid_records", nullable = false)
    private long invalidRecords;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private IngestionBatchStatus status = IngestionBatchStatus.RECEIVED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
}
