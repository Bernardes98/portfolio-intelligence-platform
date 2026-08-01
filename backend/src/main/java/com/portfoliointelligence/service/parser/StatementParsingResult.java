package com.portfoliointelligence.service.parser;

import java.util.List;

public record StatementParsingResult(

        List<ParsedInvestmentPosition> positions,
        List<String> warnings

) {

    public StatementParsingResult {
        positions = positions == null
                ? List.of()
                : List.copyOf(positions);

        warnings = warnings == null
                ? List.of()
                : List.copyOf(warnings);
    }
}