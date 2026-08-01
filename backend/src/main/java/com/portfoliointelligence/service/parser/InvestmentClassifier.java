package com.portfoliointelligence.service.parser;

import com.portfoliointelligence.entity.AssetClass;
import com.portfoliointelligence.entity.InstrumentType;
import com.portfoliointelligence.entity.InvestmentStyle;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class InvestmentClassifier {

    public AssetClass classifyAssetClass(
            String declaredClass,
            String assetName,
            String instrument
    ) {
        String value = normalize(
                declaredClass
                        + " "
                        + assetName
                        + " "
                        + instrument
        );

        if (containsAny(
                value,
                "RENDA FIXA",
                "FIXED INCOME",
                "BOND",
                "TREASURY",
                "CDB",
                "DEBENTURE",
                "CERTIFICATE OF DEPOSIT"
        )) {
            return AssetClass.FIXED_INCOME;
        }

        if (containsAny(
                value,
                "RENDA VARIAVEL",
                "EQUITY",
                "STOCK",
                "ACAO",
                "SHARE"
        )) {
            return AssetClass.VARIABLE_INCOME;
        }

        if (containsAny(
                value,
                "FUNDO",
                "FUND",
                "ETF",
                "REIT"
        )) {
            return AssetClass.FUND;
        }

        if (containsAny(
                value,
                "CASH",
                "CAIXA",
                "MONEY MARKET",
                "SALDO"
        )) {
            return AssetClass.CASH;
        }

        if (containsAny(
                value,
                "PRIVATE EQUITY",
                "HEDGE",
                "ALTERNATIVE",
                "CRYPTO",
                "BITCOIN",
                "STRUCTURED"
        )) {
            return AssetClass.ALTERNATIVE;
        }

        if (containsAny(value, "OTHER", "OUTROS")) {
            return AssetClass.OTHER;
        }

        return AssetClass.UNKNOWN;
    }

    public InstrumentType classifyInstrument(
            String declaredInstrument,
            String assetName
    ) {
        String value = normalize(
                declaredInstrument + " " + assetName
        );

        if (containsAny(
                value,
                "TREASURY",
                "BOND",
                "DEBENTURE",
                "CDB",
                "NOTE",
                "FIXED INCOME"
        )) {
            return InstrumentType.BOND;
        }

        if (containsAny(
                value,
                "STOCK",
                "EQUITY",
                "ACAO",
                "SHARE"
        )) {
            return InstrumentType.STOCK;
        }

        if (value.contains("ETF")) {
            return InstrumentType.ETF;
        }

        if (containsAny(
                value,
                "MUTUAL FUND",
                "INVESTMENT FUND",
                "FUNDO"
        )) {
            return InstrumentType.MUTUAL_FUND;
        }

        if (value.contains("REIT")) {
            return InstrumentType.REIT;
        }

        if (containsAny(
                value,
                "CASH",
                "CAIXA",
                "MONEY MARKET"
        )) {
            return InstrumentType.CASH;
        }

        if (containsAny(
                value,
                "CRYPTO",
                "BITCOIN",
                "ETHEREUM"
        )) {
            return InstrumentType.CRYPTO;
        }

        if (containsAny(
                value,
                "OPTION",
                "FUTURE",
                "DERIVATIVE",
                "SWAP"
        )) {
            return InstrumentType.DERIVATIVE;
        }

        if (containsAny(
                value,
                "STRUCTURED",
                "ESTRUTURADA",
                "COE"
        )) {
            return InstrumentType.STRUCTURED_PRODUCT;
        }

        if (containsAny(value, "OTHER", "OUTROS")) {
            return InstrumentType.OTHER;
        }

        return InstrumentType.UNKNOWN;
    }

    public InvestmentStyle classifyStyle(
            String declaredStyle,
            String assetName
    ) {
        String value = normalize(
                declaredStyle + " " + assetName
        );

        if (containsAny(
                value,
                "INCOME",
                "RENDA",
                "DIVIDEND"
        )) {
            return InvestmentStyle.INCOME;
        }

        if (containsAny(
                value,
                "GROWTH",
                "CRESCIMENTO"
        )) {
            return InvestmentStyle.GROWTH;
        }

        if (containsAny(
                value,
                "VALUE",
                "VALOR"
        )) {
            return InvestmentStyle.VALUE;
        }

        if (containsAny(
                value,
                "BLEND",
                "BALANCED",
                "BALANCEADO"
        )) {
            return InvestmentStyle.BLEND;
        }

        if (containsAny(
                value,
                "PRESERVATION",
                "CONSERVATIVE",
                "CONSERVADOR"
        )) {
            return InvestmentStyle.PRESERVATION;
        }

        if (containsAny(
                value,
                "SPECULATIVE",
                "ESPECULATIVO",
                "AGGRESSIVE",
                "AGRESSIVO"
        )) {
            return InvestmentStyle.SPECULATIVE;
        }

        if (containsAny(value, "OTHER", "OUTROS")) {
            return InvestmentStyle.OTHER;
        }

        return InvestmentStyle.UNKNOWN;
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

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer
                .normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }
}