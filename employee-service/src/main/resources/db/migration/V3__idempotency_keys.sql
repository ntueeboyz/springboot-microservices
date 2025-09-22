CREATE SCHEMA IF NOT EXISTS employee;

CREATE TABLE IF NOT EXISTS employee.idempotency_keys (
                                                         id BIGSERIAL PRIMARY KEY,
                                                         key_value VARCHAR(200) NOT NULL UNIQUE,
    request_hash VARCHAR(128) NOT NULL,
    employee_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );
