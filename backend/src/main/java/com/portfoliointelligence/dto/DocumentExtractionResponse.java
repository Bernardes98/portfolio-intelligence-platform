package com.portfoliointelligence.dto;

import com.portfoliointelligence.entity.DocumentExtraction;
import com.portfoliointelligence.entity.FinancialInstitution;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DocumentExtractionResponse(

        UUID documentId,
        FinancialInstitution institution,
        int pageCount,
        int characterCount,
        int positionCount,
        Instant extractedAt,
        Instant parsedAt,
        List<String> parsingWarnings,
        String textPreview

) {

    private static final int PREVIEW_LENGTH = 1000;

    public static DocumentExtractionResponse from(
            DocumentExtraction extraction
    ) {
        String text = extraction.getExtractedText();

        String preview = text.length() <= PREVIEW_LENGTH
                ? text
                : text.substring(
                0,
                PREVIEW_LENGTH
        ) + "...";

        return new DocumentExtractionResponse(
                extraction.getDocumentId(),
                extraction.getInstitution(),
                extraction.getPageCount(),
                extraction.getCharacterCount(),
                extraction.getPositionCount(),
                extraction.getExtractedAt(),
                extraction.getParsedAt(),
                parseWarnings(
                        extraction.getParsingWarnings()
                ),
                preview
        );
    }

    private static List<String> parseWarnings(
            String warnings
    ) {
        if (warnings == null || warnings.isBlank()) {
            return List.of();
        }

        return warnings
                .lines()
                .filter(line -> !line.isBlank())
                .toList();
    }
}