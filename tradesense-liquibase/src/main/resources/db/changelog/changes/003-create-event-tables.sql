--liquibase formatted sql

--changeset tradesense:006-create-trade-event-outbox
CREATE TABLE trade_event_outbox (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    record_id UUID NOT NULL,
    trade_id UUID,
    event_type VARCHAR(100) NOT NULL,
    aggregate_key VARCHAR(150) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW'
        CHECK (status IN ('NEW', 'PUBLISHED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ,
    CONSTRAINT fk_outbox_ingestion_record
        FOREIGN KEY (record_id) REFERENCES ingestion_record(record_id) ON DELETE RESTRICT,
    CONSTRAINT fk_outbox_trade
        FOREIGN KEY (trade_id) REFERENCES trade(id) ON DELETE RESTRICT
);

--changeset tradesense:007-create-trade-event-inbox
CREATE TABLE trade_event_inbox (
    event_id UUID NOT NULL,
    consumer_name VARCHAR(100) NOT NULL,
    processing_status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED'
        CHECK (processing_status IN ('RECEIVED', 'COMPLETED', 'FAILED')),
    retry_count INTEGER NOT NULL DEFAULT 0,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    CONSTRAINT pk_trade_event_inbox PRIMARY KEY (event_id, consumer_name)
);

--changeset tradesense:008-add-payload-to-trade-event-inbox
ALTER TABLE trade_event_inbox
    ADD COLUMN payload JSONB NOT NULL DEFAULT '{}'::jsonb;
