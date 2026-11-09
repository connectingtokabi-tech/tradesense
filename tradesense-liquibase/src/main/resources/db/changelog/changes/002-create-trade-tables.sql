--liquibase formatted sql

--changeset tradesense:003-create-trade
CREATE TABLE trade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ingestion_record_id UUID NOT NULL UNIQUE,
    booking_system VARCHAR(100) NOT NULL,
    trade_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL CHECK (source_type IN ('EOD', 'INTRADAY')),
    trade_status VARCHAR(20) NOT NULL CHECK (trade_status IN ('VALID', 'INVALID')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trade_ingestion_record
        FOREIGN KEY (ingestion_record_id) REFERENCES ingestion_record(record_id) ON DELETE RESTRICT
);

--changeset tradesense:004-create-trade-instrumentation
CREATE TABLE trade_instrumentation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trade_id UUID NOT NULL UNIQUE,
    instrument_id VARCHAR(100) NOT NULL,
    quantity BIGINT NOT NULL,
    trade_price NUMERIC(20,8) NOT NULL,
    trade_timestamp TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trade_instrumentation_trade
        FOREIGN KEY (trade_id) REFERENCES trade(id) ON DELETE CASCADE
);

--changeset tradesense:005-create-trade-validation
CREATE TABLE trade_validation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trade_id UUID NOT NULL,
    validation_status VARCHAR(20) NOT NULL CHECK (validation_status IN ('VALID', 'INVALID')),
    error_code VARCHAR(50),
    error_message VARCHAR(500),
    rule_name VARCHAR(100),
    rule_version VARCHAR(30),
    validated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trade_validation_trade
        FOREIGN KEY (trade_id) REFERENCES trade(id) ON DELETE CASCADE
);
