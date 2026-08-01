package com.portfoliointelligence.service.parser;

import com.portfoliointelligence.entity.AssetClass;
import com.portfoliointelligence.entity.FinancialInstitution;
import com.portfoliointelligence.entity.InstrumentType;
import com.portfoliointelligence.entity.InvestmentStyle;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class GenericTabularInvestmentStatementParser
        implements InvestmentStatementParser {

    private static final int MAX_HEADER_SEARCH_LINES = 150;
    private static final int MAX_CONSECUTIVE_INVALID_LINES = 8;

    private final InvestmentClassifier classifier;

    public GenericTabularInvestmentStatementParser(
            InvestmentClassifier classifier
    ) {
        this.classifier = classifier;
    }

    @Override
    public boolean supports(
            FinancialInstitution institution
    ) {
        return true;
    }

    @Override
    public StatementParsingResult parse(
            FinancialInstitution institution,
            String extractedText
    ) {
        List<String> lines = extractedText
                .lines()
                .map(String::trim)
                .toList();

        HeaderDefinition header = findHeader(lines);

        if (header == null) {
            return new StatementParsingResult(
                    List.of(),
                    List.of(
                            "Não foi possível localizar uma tabela "
                                    + "com as colunas de ativo e valor."
                    )
            );
        }

        List<ParsedInvestmentPosition> positions =
                new ArrayList<>();

        List<String> warnings =
                new ArrayList<>();

        int invalidLines = 0;
        int sequence = 0;

        for (
                int lineIndex = header.lineIndex() + 1;
                lineIndex < lines.size();
                lineIndex++
        ) {
            String line = lines.get(lineIndex);

            if (!StringUtils.hasText(line)) {
                continue;
            }

            if (isSummaryLine(line)) {
                continue;
            }

            String[] columns = header
                    .delimiter()
                    .split(line);

            String assetName = valueAt(
                    columns,
                    header.columns(),
                    ColumnType.ASSET_NAME
            );

            String marketValueText = valueAt(
                    columns,
                    header.columns(),
                    ColumnType.MARKET_VALUE
            );

            BigDecimal marketValue =
                    parseNumber(marketValueText);

            if (!StringUtils.hasText(assetName)
                    || marketValue == null) {
                invalidLines++;

                if (invalidLines
                        >= MAX_CONSECUTIVE_INVALID_LINES) {
                    break;
                }

                continue;
            }

            invalidLines = 0;
            sequence++;

            String identifier = blankToNull(
                    valueAt(
                            columns,
                            header.columns(),
                            ColumnType.IDENTIFIER
                    )
            );

            String declaredClass = valueAt(
                    columns,
                    header.columns(),
                    ColumnType.ASSET_CLASS
            );

            String declaredInstrument = valueAt(
                    columns,
                    header.columns(),
                    ColumnType.INSTRUMENT
            );

            String declaredStyle = valueAt(
                    columns,
                    header.columns(),
                    ColumnType.STYLE
            );

            BigDecimal quantity = parseNumber(
                    valueAt(
                            columns,
                            header.columns(),
                            ColumnType.QUANTITY
                    )
            );

            BigDecimal unitPrice = parseNumber(
                    valueAt(
                            columns,
                            header.columns(),
                            ColumnType.UNIT_PRICE
                    )
            );

            BigDecimal percentage = parseNumber(
                    valueAt(
                            columns,
                            header.columns(),
                            ColumnType.PORTFOLIO_PERCENTAGE
                    )
            );

            LocalDate investmentDate = parseDate(
                    valueAt(
                            columns,
                            header.columns(),
                            ColumnType.INVESTMENT_DATE
                    ),
                    institution
            );

            AssetClass assetClass =
                    classifier.classifyAssetClass(
                            declaredClass,
                            assetName,
                            declaredInstrument
                    );

            InstrumentType instrumentType =
                    classifier.classifyInstrument(
                            declaredInstrument,
                            assetName
                    );

            InvestmentStyle investmentStyle =
                    classifier.classifyStyle(
                            declaredStyle,
                            assetName
                    );

            positions.add(
                    new ParsedInvestmentPosition(
                            assetName,
                            identifier,
                            assetClass,
                            instrumentType,
                            investmentStyle,
                            quantity,
                            unitPrice,
                            marketValue,
                            percentage,
                            investmentDate,
                            sequence,
                            calculateSourceHash(
                                    sequence,
                                    line
                            ),
                            truncateSourceLine(line)
                    )
            );
        }

        if (positions.isEmpty()) {
            warnings.add(
                    "A tabela foi localizada, mas nenhuma posição "
                            + "financeira válida foi reconhecida."
            );
        }

        return new StatementParsingResult(
                positions,
                warnings
        );
    }

    private HeaderDefinition findHeader(
            List<String> lines
    ) {
        HeaderDefinition bestHeader = null;
        int bestScore = 0;

        int limit = Math.min(
                lines.size(),
                MAX_HEADER_SEARCH_LINES
        );

        for (int lineIndex = 0; lineIndex < limit; lineIndex++) {
            String line = lines.get(lineIndex);

            for (Delimiter delimiter : Delimiter.values()) {
                String[] columns = delimiter.split(line);

                if (columns.length < 2) {
                    continue;
                }

                Map<ColumnType, Integer> columnMapping =
                        buildColumnMapping(columns);

                if (!columnMapping.containsKey(
                        ColumnType.ASSET_NAME
                )) {
                    continue;
                }

                if (!columnMapping.containsKey(
                        ColumnType.MARKET_VALUE
                )) {
                    continue;
                }

                int score = columnMapping.size();

                if (score > bestScore) {
                    bestScore = score;
                    bestHeader = new HeaderDefinition(
                            lineIndex,
                            delimiter,
                            columnMapping
                    );
                }
            }
        }

        return bestHeader;
    }

    private Map<ColumnType, Integer> buildColumnMapping(
            String[] columns
    ) {
        Map<ColumnType, Integer> mapping =
                new EnumMap<>(ColumnType.class);

        for (int index = 0; index < columns.length; index++) {
            ColumnType type = identifyColumn(columns[index]);

            if (type != null) {
                mapping.putIfAbsent(type, index);
            }
        }

        return mapping;
    }

    private ColumnType identifyColumn(String columnName) {
        String value = normalizeHeader(columnName);

        if (containsAny(
                value,
                "CLASSE DE ATIVO",
                "ASSET CLASS",
                "ASSET CATEGORY",
                "CATEGORIA"
        )) {
            return ColumnType.ASSET_CLASS;
        }

        if (containsAny(
                value,
                "VALOR EM USD",
                "VALOR USD",
                "MARKET VALUE",
                "CURRENT VALUE",
                "VALUE USD",
                "MARKET VALUE USD"
        )) {
            return ColumnType.MARKET_VALUE;
        }

        if (containsAny(
                value,
                "PERCENT DO PORTFOLIO",
                "PORTFOLIO PERCENT",
                "PORTFOLIO %",
                "ALLOCATION",
                "WEIGHT",
                "PERCENTAGE"
        )) {
            return ColumnType.PORTFOLIO_PERCENTAGE;
        }

        if (containsAny(
                value,
                "DATA DO INVESTIMENTO",
                "INVESTMENT DATE",
                "ACQUISITION DATE",
                "PURCHASE DATE"
        )) {
            return ColumnType.INVESTMENT_DATE;
        }

        if (containsAny(
                value,
                "PRECO UNITARIO",
                "UNIT PRICE",
                "PRICE USD"
        )) {
            return ColumnType.UNIT_PRICE;
        }

        if (containsAny(
                value,
                "QUANTIDADE",
                "QUANTITY",
                "SHARES",
                "UNITS"
        )) {
            return ColumnType.QUANTITY;
        }

        if (containsAny(
                value,
                "IDENTIFICADOR",
                "IDENTIFIER",
                "TICKER",
                "CUSIP",
                "ISIN",
                "SYMBOL"
        )) {
            return ColumnType.IDENTIFIER;
        }

        if (containsAny(
                value,
                "INSTRUMENTO",
                "INSTRUMENT",
                "PRODUCT TYPE",
                "SECURITY TYPE"
        )) {
            return ColumnType.INSTRUMENT;
        }

        if (containsAny(
                value,
                "ESTILO",
                "STYLE",
                "STRATEGY"
        )) {
            return ColumnType.STYLE;
        }

        if (containsAny(
                value,
                "ATIVO",
                "ASSET",
                "SECURITY",
                "DESCRIPTION",
                "INVESTMENT"
        )) {
            return ColumnType.ASSET_NAME;
        }

        return null;
    }

    private String valueAt(
            String[] values,
            Map<ColumnType, Integer> mapping,
            ColumnType type
    ) {
        Integer index = mapping.get(type);

        if (index == null || index >= values.length) {
            return null;
        }

        String value = values[index];

        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private BigDecimal parseNumber(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return null;
        }

        String value = rawValue
                .replace('\u00A0', ' ')
                .toUpperCase(Locale.ROOT)
                .replace("USD", "")
                .replace("US$", "")
                .replace("R$", "")
                .replace("$", "")
                .replace("%", "")
                .replace(" ", "")
                .trim();

        boolean negative =
                value.startsWith("(")
                        && value.endsWith(")");

        value = value
                .replace("(", "")
                .replace(")", "")
                .replaceAll("[^0-9,.-]", "");

        if (!StringUtils.hasText(value)
                || value.equals("-")) {
            return null;
        }

        value = normalizeDecimalSeparators(value);

        try {
            BigDecimal result = new BigDecimal(value);

            return negative
                    ? result.negate()
                    : result;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String normalizeDecimalSeparators(
            String value
    ) {
        int lastComma = value.lastIndexOf(',');
        int lastDot = value.lastIndexOf('.');

        if (lastComma >= 0 && lastDot >= 0) {
            if (lastComma > lastDot) {
                return value
                        .replace(".", "")
                        .replace(',', '.');
            }

            return value.replace(",", "");
        }

        if (lastComma >= 0) {
            int decimalPlaces =
                    value.length() - lastComma - 1;

            if (decimalPlaces == 3
                    && value.indexOf(',') == lastComma) {
                return value.replace(",", "");
            }

            return value.replace(',', '.');
        }

        if (lastDot >= 0) {
            int decimalPlaces =
                    value.length() - lastDot - 1;

            if (decimalPlaces == 3
                    && value.indexOf('.') == lastDot) {
                return value.replace(".", "");
            }

            if (value.indexOf('.') != lastDot) {
                String integerPart = value
                        .substring(0, lastDot)
                        .replace(".", "");

                return integerPart
                        + value.substring(lastDot);
            }
        }

        return value;
    }

    private LocalDate parseDate(
            String rawValue,
            FinancialInstitution institution
    ) {
        if (!StringUtils.hasText(rawValue)) {
            return null;
        }

        List<DateTimeFormatter> formatters =
                preferredDateFormatters(institution);

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(
                        rawValue.trim(),
                        formatter
                );
            } catch (DateTimeParseException ignored) {
                // Testa o próximo formato.
            }
        }

        return null;
    }

    private List<DateTimeFormatter>
    preferredDateFormatters(
            FinancialInstitution institution
    ) {
        DateTimeFormatter dayFirst =
                DateTimeFormatter.ofPattern(
                        "d/M/uuuu"
                );

        DateTimeFormatter monthFirst =
                DateTimeFormatter.ofPattern(
                        "M/d/uuuu"
                );

        DateTimeFormatter iso =
                DateTimeFormatter.ISO_LOCAL_DATE;

        DateTimeFormatter americanText =
                DateTimeFormatter.ofPattern(
                        "MMM d, uuuu",
                        Locale.US
                );

        boolean americanInstitution =
                institution
                        == FinancialInstitution.AVENUE
                        || institution
                        == FinancialInstitution.CHARLES_SCHWAB
                        || institution
                        == FinancialInstitution.PERSHING
                        || institution
                        == FinancialInstitution.MORGAN_STANLEY;

        if (americanInstitution) {
            return List.of(
                    monthFirst,
                    iso,
                    dayFirst,
                    americanText
            );
        }

        return List.of(
                dayFirst,
                iso,
                monthFirst,
                americanText
        );
    }

    private String calculateSourceHash(
            int sequence,
            String sourceLine
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] bytes = (
                    sequence
                            + "|"
                            + sourceLine.trim()
            ).getBytes(StandardCharsets.UTF_8);

            return HexFormat
                    .of()
                    .formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 não está disponível.",
                    exception
            );
        }
    }

    private boolean isSummaryLine(String line) {
        String value = normalizeHeader(line);

        return value.startsWith("TOTAL")
                || value.startsWith("SUBTOTAL")
                || value.startsWith("PORTFOLIO TOTAL")
                || value.startsWith("TOTAL PORTFOLIO");
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private String truncateSourceLine(String value) {
        if (value.length() <= 2000) {
            return value;
        }

        return value.substring(0, 2000);
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer
                .normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("%", " PERCENT ")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsAny(
            String value,
            String... terms
    ) {
        for (String term : terms) {
            if (value.contains(term)) {
                return true;
            }
        }

        return false;
    }

    private enum ColumnType {

        ASSET_NAME,
        IDENTIFIER,
        ASSET_CLASS,
        INSTRUMENT,
        STYLE,
        QUANTITY,
        UNIT_PRICE,
        MARKET_VALUE,
        PORTFOLIO_PERCENTAGE,
        INVESTMENT_DATE
    }

    private enum Delimiter {

        PIPE("\\s*\\|\\s*"),
        SEMICOLON("\\s*;\\s*"),
        TAB("\\t+"),
        MULTIPLE_SPACES("\\s{2,}");

        private final String expression;

        Delimiter(String expression) {
            this.expression = expression;
        }

        String[] split(String value) {
            return value.split(expression, -1);
        }
    }

    private record HeaderDefinition(
            int lineIndex,
            Delimiter delimiter,
            Map<ColumnType, Integer> columns
    ) {
    }
}