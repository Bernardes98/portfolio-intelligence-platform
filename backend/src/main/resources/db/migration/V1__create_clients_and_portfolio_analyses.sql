CREATE TABLE clients (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    document_number VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uk_clients_email_lower ON clients (LOWER(email));
CREATE UNIQUE INDEX uk_clients_document_number ON clients (document_number);

CREATE TABLE portfolio_analyses (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_documents INTEGER NOT NULL DEFAULT 0,
    total_assets INTEGER NOT NULL DEFAULT 0,
    total_portfolio_value NUMERIC(19, 4) NOT NULL DEFAULT 0,
    error_message VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_portfolio_analyses_client
        FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT ck_portfolio_analyses_total_documents
        CHECK (total_documents >= 0 AND total_documents <= 6),
    CONSTRAINT ck_portfolio_analyses_total_assets
        CHECK (total_assets >= 0),
    CONSTRAINT ck_portfolio_analyses_total_value
        CHECK (total_portfolio_value >= 0)
);

CREATE INDEX idx_portfolio_analyses_client_created_at
    ON portfolio_analyses (client_id, created_at DESC);
CREATE INDEX idx_portfolio_analyses_status
    ON portfolio_analyses (status);
