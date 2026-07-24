package com.portfoliointelligence.dto;

import com.portfoliointelligence.entity.DocumentStatus;
import com.portfoliointelligence.entity.InvestmentDocument;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(

        UUID id,
        UUID analysisId,
        String originalFilename,
        String contentType,
        long fileSize,
        String checksum,
        DocumentStatus status,
        String errorMessage,
        Instant createdAt,
        Instant processedAt

) {

    public static DocumentResponse from(
            InvestmentDocument document
    ) {
        return new DocumentResponse(
                document.getId(),
                document.getAnalysisId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getFileSize(),
                document.getChecksum(),
                document.getStatus(),
                document.getErrorMessage(),
                document.getCreatedAt(),
                document.getProcessedAt()
        );
    }
}