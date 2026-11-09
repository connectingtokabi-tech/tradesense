--liquibase formatted sql

--changeset tradesense:009-add-traceparent-to-ingestion-records
ALTER TABLE ingestion_batch
    ADD COLUMN traceparent VARCHAR(55);

ALTER TABLE ingestion_record
    ADD COLUMN traceparent VARCHAR(55);
