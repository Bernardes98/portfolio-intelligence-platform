ALTER TABLE document_extractions
    ADD COLUMN position_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN parsing_warnings TEXT,
    ADD COLUMN parsed_at TIMESTAMPTZ;

ALTER TABLE document_extractions
    ADD CONSTRAINT ck_document_extractions_position_count
        CHECK (position_count >= 0);

CREATE TABLE investment_positions (
                                      id UUID PRIMARY KEY,
                                      analysis_id UUID NOT NULL,
                                      document_id UUID NOT NULL,
                                      institution VARCHAR(40) NOT NULL,
                                      asset_name VARCHAR(500) NOT NULL,
                                      identifier VARCHAR(150),
                                      asset_class VARCHAR(40) NOT NULL,
                                      instrument_type VARCHAR(50) NOT NULL,
                                      investment_style VARCHAR(50) NOT NULL,
                                      quantity NUMERIC(24, 8),
                                      unit_price_usd NUMERIC(19, 6),
                                      market_value_usd NUMERIC(19, 4) NOT NULL,
                                      portfolio_percentage NUMERIC(12, 6),
                                      investment_date DATE,
                                      source_sequence INTEGER NOT NULL,
                                      source_hash VARCHAR(64) NOT NULL,
                                      source_line VARCHAR(2000) NOT NULL,
                                      created_at TIMESTAMPTZ NOT NULL,

                                      CONSTRAINT fk_investment_positions_analysis
                                          FOREIGN KEY (analysis_id)
                                              REFERENCES portfolio_analyses (id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_investment_positions_document
                                          FOREIGN KEY (document_id)
                                              REFERENCES investment_documents (id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT uk_investment_positions_document_source
                                          UNIQUE (document_id, source_hash),

                                      CONSTRAINT ck_investment_positions_institution
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
                                              ),

                                      CONSTRAINT ck_investment_positions_asset_class
                                          CHECK (
                                              asset_class IN (
                                                              'FIXED_INCOME',
                                                              'VARIABLE_INCOME',
                                                              'FUND',
                                                              'CASH',
                                                              'ALTERNATIVE',
                                                              'OTHER',
                                                              'UNKNOWN'
                                                  )
                                              ),

                                      CONSTRAINT ck_investment_positions_instrument_type
                                          CHECK (
                                              instrument_type IN (
                                                                  'BOND',
                                                                  'STOCK',
                                                                  'ETF',
                                                                  'MUTUAL_FUND',
                                                                  'REIT',
                                                                  'CASH',
                                                                  'CRYPTO',
                                                                  'DERIVATIVE',
                                                                  'STRUCTURED_PRODUCT',
                                                                  'OTHER',
                                                                  'UNKNOWN'
                                                  )
                                              ),

                                      CONSTRAINT ck_investment_positions_investment_style
                                          CHECK (
                                              investment_style IN (
                                                                   'INCOME',
                                                                   'GROWTH',
                                                                   'VALUE',
                                                                   'BLEND',
                                                                   'PRESERVATION',
                                                                   'SPECULATIVE',
                                                                   'OTHER',
                                                                   'UNKNOWN'
                                                  )
                                              ),

                                      CONSTRAINT ck_investment_positions_source_sequence
                                          CHECK (source_sequence > 0),

                                      CONSTRAINT ck_investment_positions_percentage
                                          CHECK (
                                              portfolio_percentage IS NULL
                                                  OR (
                                                  portfolio_percentage >= 0
                                                      AND portfolio_percentage <= 100
                                                  )
                                              )
);

CREATE INDEX idx_investment_positions_analysis
    ON investment_positions (analysis_id);

CREATE INDEX idx_investment_positions_document
    ON investment_positions (document_id);

CREATE INDEX idx_investment_positions_identifier
    ON investment_positions (identifier);

CREATE INDEX idx_investment_positions_asset_class
    ON investment_positions (asset_class);

CREATE INDEX idx_investment_positions_institution
    ON investment_positions (institution);