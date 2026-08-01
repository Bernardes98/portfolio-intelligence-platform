package com.portfoliointelligence.service.parser;

import com.portfoliointelligence.entity.FinancialInstitution;

public interface InvestmentStatementParser {

    boolean supports(FinancialInstitution institution);

    StatementParsingResult parse(
            FinancialInstitution institution,
            String extractedText
    );

    default int priority() {
        return 0;
    }
}