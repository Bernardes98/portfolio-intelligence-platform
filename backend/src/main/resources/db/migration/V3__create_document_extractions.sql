ALTER TABLE investment_documents
    ADD COLUMN processing_started_at TIMESTAMPTZ,
    ADD COLUMN processing_attempts INTEGER NOT NULL DEFAULT 0;

ALTER TABLE investment_documents
    ADD CONSTRAINT ck_investment_documents_processing_attempts
        CHECK (processing_attempts >= 0);

CREATE TABLE document_extractions (
                                      document_id UUID PRIMARY KEY,
                                      institution VARCHAR(40) NOT NULL,
                                      page_count INTEGER NOT NULL,
                                      extracted_text TEXT NOT NULL,
                                      character_count INTEGER NOT NULL,
                                      extracted_at TIMESTAMPTZ NOT NULL,

                                      CONSTRAINT fk_document_extractions_document
                                          FOREIGN KEY (document_id)
                                              REFERENCES investment_documents (id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT ck_document_extractions_page_count
                                          CHECK (page_count > 0),

                                      CONSTRAINT ck_document_extractions_character_count
                                          CHECK (character_count >= 0),

                                      CONSTRAINT ck_document_extractions_institution
                                          CHECK (
                                              institution IN (
                                                              'XP',
                                                              'BTG_PACTUAL',
                                                              'AVENUE',
                                                              'CHARLES_SCHWAB',
                                                              'PERSHING',
                                                              'MORGAN_STANLEY',
                                                              'UNKNOWN'
                                                  )
                                              )
);

CREATE INDEX idx_document_extractions_institution
    ON document_extractions (institution);