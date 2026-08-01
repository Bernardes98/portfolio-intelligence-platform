package com.portfoliointelligence.service.parser;

import com.portfoliointelligence.entity.AssetClass;
import com.portfoliointelligence.entity.FinancialInstitution;
import com.portfoliointelligence.entity.InstrumentType;
import com.portfoliointelligence.entity.InvestmentStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GenericTabularInvestmentStatementParserTest {

    private GenericTabularInvestmentStatementParser parser;

    @BeforeEach
    void setUp() {
        parser = new GenericTabularInvestmentStatementParser(
                new InvestmentClassifier()
        );
    }

    @Test
    void shouldParseTabularInvestmentPositions() {
        String extractedText = """
                XP INVESTIMENTOS

                Asset | Identifier | Asset Class | Instrument | Style | Quantity | Unit Price | Market Value | Portfolio % | Investment Date
                Apple Inc | AAPL | Equity | Stock | Growth | 100 | 200.00 | 20000.00 | 40.00% | 31/07/2026
                US Treasury Bond | US912810 | Fixed Income | Bond | Income | 300 | 100.00 | 30000.00 | 60.00% | 01/07/2026
                Total | | | | | | | 50000.00 | 100.00% |
                """;

        StatementParsingResult result = parser.parse(
                FinancialInstitution.XP,
                extractedText
        );

        assertThat(result.positions()).hasSize(2);
        assertThat(result.warnings()).isEmpty();

        ParsedInvestmentPosition apple =
                result.positions().getFirst();

        assertThat(apple.assetName())
                .isEqualTo("Apple Inc");

        assertThat(apple.identifier())
                .isEqualTo("AAPL");

        assertThat(apple.assetClass())
                .isEqualTo(
                        AssetClass.VARIABLE_INCOME
                );

        assertThat(apple.instrumentType())
                .isEqualTo(
                        InstrumentType.STOCK
                );

        assertThat(apple.investmentStyle())
                .isEqualTo(
                        InvestmentStyle.GROWTH
                );

        assertThat(apple.quantity())
                .isEqualByComparingTo(
                        new BigDecimal("100")
                );

        assertThat(apple.marketValueUsd())
                .isEqualByComparingTo(
                        new BigDecimal("20000.00")
                );

        assertThat(apple.portfolioPercentage())
                .isEqualByComparingTo(
                        new BigDecimal("40.00")
                );

        assertThat(apple.investmentDate())
                .isEqualTo(
                        LocalDate.of(
                                2026,
                                7,
                                31
                        )
                );
    }

    @Test
    void shouldReturnWarningWhenTableIsNotFound() {
        StatementParsingResult result = parser.parse(
                FinancialInstitution.UNKNOWN,
                "Documento sem tabela de investimentos."
        );

        assertThat(result.positions()).isEmpty();
        assertThat(result.warnings()).isNotEmpty();
    }
}