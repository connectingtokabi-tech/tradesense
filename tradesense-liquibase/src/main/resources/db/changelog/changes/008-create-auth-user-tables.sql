--liquibase formatted sql

--changeset tradesense:016-create-app-user
CREATE TABLE app_user (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    account_expired BOOLEAN NOT NULL DEFAULT FALSE,
    credentials_expired BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_app_user_username UNIQUE (username)
);

--changeset tradesense:017-create-app-authority
CREATE TABLE app_authority (
    user_id UUID NOT NULL,
    authority VARCHAR(100) NOT NULL,
    CONSTRAINT pk_app_authority PRIMARY KEY (user_id, authority),
    CONSTRAINT fk_app_authority_user
        FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE
);

--rollback DROP TABLE IF EXISTS app_authority CASCADE;
--rollback DROP TABLE IF EXISTS app_user CASCADE;

