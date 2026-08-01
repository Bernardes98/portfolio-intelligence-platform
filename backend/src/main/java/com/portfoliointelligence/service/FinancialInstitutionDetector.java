package com.portfoliointelligence.service;

import com.portfoliointelligence.entity.FinancialInstitution;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class FinancialInstitutionDetector {

    private static final Map<
            FinancialInstitution,
            List<String>
            > SIGNATURES = createSignatures();

    public FinancialInstitution detect(String extractedText) {
        String normalizedText = normalize(extractedText);

        return SIGNATURES.entrySet()
                .stream()
                .filter(entry ->
                        entry.getValue()
                                .stream()
                                .anyMatch(normalizedText::contains)
                )
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(FinancialInstitution.UNKNOWN);
    }

    private static Map<
            FinancialInstitution,
            List<String>
            > createSignatures() {

        Map<FinancialInstitution, List<String>> signatures =
                new LinkedHashMap<>();

        signatures.put(
                FinancialInstitution.XP,
                List.of(
                        "XP INVESTIMENTOS",
                        "XP SECURITIES",
                        "XP INC"
                )
        );

        signatures.put(
                FinancialInstitution.BTG_PACTUAL,
                List.of(
                        "BTG PACTUAL",
                        "BANCO BTG"
                )
        );

        signatures.put(
                FinancialInstitution.AVENUE,
                List.of(
                        "AVENUE SECURITIES",
                        "AVENUE US"
                )
        );

        signatures.put(
                FinancialInstitution.CHARLES_SCHWAB,
                List.of(
                        "CHARLES SCHWAB",
                        "SCHWAB ONE"
                )
        );

        signatures.put(
                FinancialInstitution.PERSHING,
                List.of(
                        "PERSHING LLC",
                        "PERSHING ADVISOR SOLUTIONS"
                )
        );

        signatures.put(
                FinancialInstitution.MORGAN_STANLEY,
                List.of(
                        "MORGAN STANLEY"
                )
        );

        return signatures;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String withoutAccents = Normalizer
                .normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return withoutAccents.toUpperCase(Locale.ROOT);
    }
}