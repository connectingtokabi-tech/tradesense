-- PostgreSQL script to drop all application tables and indexes.
-- Run with: psql -U tradesense -d tradesense -f drop-all-tables.sql

-- Drop application indexes first so they are removed cleanly before table cleanup.
DROP INDEX IF EXISTS public.idx_outbox_status_created;
DROP INDEX IF EXISTS public.idx_inbox_status_received;
DROP INDEX IF EXISTS public.idx_trade_validation_error;
DROP INDEX IF EXISTS public.idx_instrumentation_instrument_timestamp;
DROP INDEX IF EXISTS public.idx_instrumentation_timestamp;
DROP INDEX IF EXISTS public.idx_trade_booking_trade;
DROP INDEX IF EXISTS public.idx_trade_status_source;
DROP INDEX IF EXISTS public.uk_ingestion_record_batch_row;
DROP INDEX IF EXISTS public.uk_ingestion_record_source_event;
DROP INDEX IF EXISTS public.uk_ingestion_batch_file_checksum;
DROP INDEX IF EXISTS public.idx_ingestion_record_status;
DROP INDEX IF EXISTS public.idx_ingestion_record_batch_row;

-- Drop Spring Batch metadata tables and sequences.
DROP TABLE IF EXISTS public.batch_job_execution_context CASCADE;
DROP TABLE IF EXISTS public.batch_step_execution_context CASCADE;
DROP TABLE IF EXISTS public.batch_job_execution_params CASCADE;
DROP TABLE IF EXISTS public.batch_step_execution CASCADE;
DROP TABLE IF EXISTS public.batch_job_execution CASCADE;
DROP TABLE IF EXISTS public.batch_job_instance CASCADE;

DROP SEQUENCE IF EXISTS public.batch_step_execution_seq CASCADE;
DROP SEQUENCE IF EXISTS public.batch_job_execution_seq CASCADE;
DROP SEQUENCE IF EXISTS public.batch_job_instance_seq CASCADE;

-- Drop application tables in dependency order.
DROP TABLE IF EXISTS public.trade_event_inbox CASCADE;
DROP TABLE IF EXISTS public.trade_event_outbox CASCADE;
DROP TABLE IF EXISTS public.trade_validation CASCADE;
DROP TABLE IF EXISTS public.trade_instrumentation CASCADE;
DROP TABLE IF EXISTS public.trade CASCADE;
DROP TABLE IF EXISTS public.ingestion_record CASCADE;
DROP TABLE IF EXISTS public.ingestion_batch CASCADE;

-- Drop authentication tables.
DROP TABLE IF EXISTS public.app_authority CASCADE;
DROP TABLE IF EXISTS public.app_user CASCADE;

-- Drop OAuth2 tables.
DROP TABLE IF EXISTS public.oauth2_authorization_consent CASCADE;
DROP TABLE IF EXISTS public.oauth2_authorization CASCADE;
DROP TABLE IF EXISTS public.oauth2_registered_client CASCADE;

-- Optional: drop extension if you want to fully recreate the database from scratch.
-- DROP EXTENSION IF EXISTS public.pgcrypto;
