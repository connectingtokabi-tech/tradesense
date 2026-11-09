--liquibase formatted sql

--changeset tradesense:008-create-ingestion-indexes
CREATE INDEX idx_ingestion_record_batch_row
    ON ingestion_record (batch_id, source_row_number);

CREATE INDEX idx_ingestion_record_status
    ON ingestion_record (source_type, ingestion_status);

CREATE UNIQUE INDEX uk_ingestion_batch_file_checksum
    ON ingestion_batch (file_checksum)
    WHERE file_checksum IS NOT NULL;

CREATE UNIQUE INDEX uk_ingestion_record_source_event
    ON ingestion_record (source_type, source_system, source_event_id)
    WHERE source_event_id IS NOT NULL;

CREATE UNIQUE INDEX uk_ingestion_record_batch_row
    ON ingestion_record (batch_id, source_row_number)
    WHERE batch_id IS NOT NULL AND source_row_number IS NOT NULL;

--changeset tradesense:009-create-trade-reporting-indexes
CREATE INDEX idx_trade_status_source
    ON trade (trade_status, source_type);

CREATE INDEX idx_trade_booking_trade
    ON trade (booking_system, trade_id);

CREATE INDEX idx_instrumentation_timestamp
    ON trade_instrumentation (trade_timestamp);

CREATE INDEX idx_instrumentation_instrument_timestamp
    ON trade_instrumentation (instrument_id, trade_timestamp);

CREATE INDEX idx_trade_validation_error
    ON trade_validation (error_code, trade_id);

--changeset tradesense:010-create-event-indexes
CREATE INDEX idx_outbox_status_created
    ON trade_event_outbox (status, created_at);

CREATE INDEX idx_inbox_status_received
    ON trade_event_inbox (processing_status, received_at);
