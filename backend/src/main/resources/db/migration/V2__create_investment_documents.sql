CREATE TABLE investment_documents (
                                      id UUID PRIMARY KEY,
                                      analysis_id UUID NOT NULL,
                                      original_filename VARCHAR(255) NOT NULL,
                                      storage_filename VARCHAR(255) NOT NULL,
                                      storage_path VARCHAR(1000) NOT NULL,
                                      content_type VARCHAR(100) NOT NULL,
                                      file_size BIGINT NOT NULL,
                                      checksum VARCHAR(64) NOT NULL,
                                      status VARCHAR(30) NOT NULL,
                                      error_message VARCHAR(1000),
                                      created_at TIMESTAMPTZ NOT NULL,
                                      processed_at TIMESTAMPTZ,

                                      CONSTRAINT fk_investment_documents_analysis
                                          FOREIGN KEY (analysis_id)
                                              REFERENCES portfolio_analyses (id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT uk_investment_documents_analysis_checksum
                                          UNIQUE (analysis_id, checksum),

                                      CONSTRAINT ck_investment_documents_file_size
                                          CHECK (file_size > 0),

                                      CONSTRAINT ck_investment_documents_status
                                          CHECK (
                                              status IN (
                                                         'UPLOADED',
                                                         'QUEUED',
                                                         'PROCESSING',
                                                         'PROCESSED',
                                                         'FAILED'
                                                  )
                                              )
);

CREATE INDEX idx_investment_documents_analysis_created_at
    ON investment_documents (analysis_id, created_at);

CREATE INDEX idx_investment_documents_status
    ON investment_documents (status);