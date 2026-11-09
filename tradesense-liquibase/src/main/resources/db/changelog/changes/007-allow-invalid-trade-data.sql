--liquibase formatted sql

--changeset tradesense:010-allow-invalid-trade-data-for-validation
ALTER TABLE trade
    ALTER COLUMN booking_system DROP NOT NULL,
    ALTER COLUMN trade_id DROP NOT NULL,
    ALTER COLUMN source_type DROP NOT NULL;

ALTER TABLE trade_instrumentation
    ALTER COLUMN instrument_id DROP NOT NULL,
    ALTER COLUMN quantity DROP NOT NULL,
    ALTER COLUMN trade_price DROP NOT NULL,
    ALTER COLUMN trade_timestamp DROP NOT NULL;
