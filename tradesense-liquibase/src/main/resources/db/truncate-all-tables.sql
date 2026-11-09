-- PostgreSQL script to clear all application and Spring Batch data.
-- Run with: psql -U tradesense -d tradesense -f truncate-all-tables.sql

TRUNCATE TABLE
    batch_job_execution_context,
    batch_step_execution_context,
    batch_job_execution_params,
    batch_step_execution,
    batch_job_execution,
    batch_job_instance,
    trade_validation,
    trade_event_inbox,
    trade_event_outbox,
    trade_instrumentation,
    trade,
    ingestion_record,
    ingestion_batch
RESTART IDENTITY CASCADE;

