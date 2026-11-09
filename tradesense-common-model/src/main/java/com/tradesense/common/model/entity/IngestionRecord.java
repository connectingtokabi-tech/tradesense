package com.tradesense.common.model.entity;

import com.tradesense.common.model.entity.enums.IngestionRecordStatus;
import com.tradesense.common.model.entity.enums.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ingestion_record")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngestionRecord {

    public static IngestionRecord forEodFileRow(
            IngestionBatch batch,
            String sourceSystem,
            long sourceRowNumber,
            JsonNode rawPayload) {
        if (batch == null || rawPayload == null || rawPayload.isNull()) {
            throw new IllegalArgumentException("EOD batch and raw payload are required");
        }
        if (batch.getSourceType() != SourceType.EOD) {
            throw new IllegalArgumentException("File rows can only be stored in an EOD batch");
        }
        if (sourceRowNumber < 1) {
            throw new IllegalArgumentException("EOD source row number must be positive");
        }
        IngestionRecord record = new IngestionRecord();
        record.batch = batch;
        record.sourceType = SourceType.EOD;
        record.sourceSystem = sourceSystem;
        record.sourceRowNumber = sourceRowNumber;
        record.sourceEventId = batch.getBatchId() + "-" + sourceRowNumber;
        record.rawPayload = rawPayload;
        record.traceparent = batch.getTraceparent();
        return record;
    }

    public static IngestionRecord forIntradayMessage(
            String sourceSystem, String sourceEventId, JsonNode rawPayload) {
        return forIntradayMessage(null, sourceSystem, sourceEventId, rawPayload);
    }

    public static IngestionRecord forIntradayMessage(
            IngestionBatch batch, String sourceSystem, String sourceEventId, JsonNode rawPayload) {
        if (batch != null && batch.getSourceType() != SourceType.INTRADAY) {
            throw new IllegalArgumentException("Intraday message must use an INTRADAY batch");
        }
        if (sourceEventId == null || sourceEventId.isBlank()) {
            throw new IllegalArgumentException("Intraday source event ID is required");
        }
        if (rawPayload == null || rawPayload.isNull()) {
            throw new IllegalArgumentException("Intraday raw payload is required");
        }
        IngestionRecord record = new IngestionRecord();
        record.batch = batch;
        record.sourceType = SourceType.INTRADAY;
        record.sourceSystem = sourceSystem;
        record.sourceEventId = sourceEventId;
        record.sourceRowNumber = null;
        record.rawPayload = rawPayload;
        record.traceparent = batch == null ? null : batch.getTraceparent();
        return record;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "record_id", nullable = false, updatable = false)
    private UUID recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", foreignKey = @ForeignKey(name = "fk_ingestion_record_batch"))
    private IngestionBatch batch;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20, nullable = false)
    private SourceType sourceType;

    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    @Column(name = "source_event_id", length = 150)
    private String sourceEventId;

    @Column(name = "source_row_number")
    private Long sourceRowNumber;

    @Column(name = "traceparent", length = 55)
    private String traceparent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "jsonb", nullable = false)
    private JsonNode rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "ingestion_status", length = 30, nullable = false)
    private IngestionRecordStatus ingestionStatus = IngestionRecordStatus.RECEIVED;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
