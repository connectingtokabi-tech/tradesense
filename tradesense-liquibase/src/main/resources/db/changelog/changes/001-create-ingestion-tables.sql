--liquibase formatted sql

--changeset tradesense:000-enable-pgcrypto
CREATE EXTENSION IF NOT EXISTS pgcrypto;

--changeset tradesense:001-create-ingestion-batch
CREATE TABLE ingestion_batch (
    batch_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_name VARCHAR(150),
    source_type VARCHAR(20) NOT NULL CHECK (source_type IN ('EOD', 'INTRADAY')),
    file_name VARCHAR(255),
    file_location VARCHAR(500),
    file_checksum VARCHAR(128),
    total_records BIGINT NOT NULL DEFAULT 0,
    processed_records BIGINT NOT NULL DEFAULT 0,
    valid_records BIGINT NOT NULL DEFAULT 0,
    invalid_records BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED'
        CHECK (status IN ('RECEIVED', 'PROCESSING', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ
);

--changeset tradesense:002-create-ingestion-record
CREATE TABLE ingestion_record (
    record_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID,
    source_type VARCHAR(20) NOT NULL CHECK (source_type IN ('EOD', 'INTRADAY')),
    source_system VARCHAR(100),
    source_event_id VARCHAR(150),
    source_row_number BIGINT,
    raw_payload JSONB NOT NULL,
    ingestion_status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED'
        CHECK (ingestion_status IN ('RECEIVED', 'PUBLISHED', 'PROCESSED', 'FAILED')),
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ingestion_record_batch
        FOREIGN KEY (batch_id) REFERENCES ingestion_batch(batch_id) ON DELETE RESTRICT
);
