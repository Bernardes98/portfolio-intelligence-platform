package com.portfoliointelligence.service.parser;

import com.portfoliointelligence.entity.FinancialInstitution;
import com.portfoliointelligence.exception.DocumentProcessingException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class InvestmentParserService {

    private final List<InvestmentStatementParser> parsers;

    public InvestmentParserService(
            List<InvestmentStatementParser> parsers
    ) {
        this.parsers = parsers
                .stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        InvestmentStatementParser::priority
                                )
                                .reversed()
                )
                .toList();
    }

    public StatementParsingResult parse(
            FinancialInstitution institution,
            String extractedText
    ) {
        InvestmentStatementParser parser = parsers
                .stream()
                .filter(candidate ->
                        candidate.supports(institution)
                )
                .findFirst()
                .orElseThrow(() ->
                        new DocumentProcessingException(
                                "Não existe parser disponível "
                                        + "para a instituição "
                                        + institution
                                        + "."
                        )
                );

        StatementParsingResult result =
                parser.parse(
                        institution,
                        extractedText
                );

        if (result.positions().isEmpty()) {
            String warning = result.warnings().isEmpty()
                    ? "Nenhuma posição financeira foi encontrada."
                    : String.join(
                            " ",
                            result.warnings()
                    );

            throw new DocumentProcessingException(warning);
        }

        return result;
    }
}