package com.portfoliointelligence.dto;

import com.portfoliointelligence.entity.AnalysisStatus;
import com.portfoliointelligence.entity.PortfolioAnalysis;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AnalysisResponse(
        UUID id,
        UUID clientId,
        AnalysisStatus status,
        int totalDocuments,
        int totalAssets,
        BigDecimal totalPortfolioValue,
        String errorMessage,
        Instant createdAt,
        Instant startedAt,
        Instant completedAt
) {
    public static AnalysisResponse from(PortfolioAnalysis analysis) {
        return new AnalysisResponse(
                analysis.getId(),
                analysis.getClientId(),
                analysis.getStatus(),
                analysis.getTotalDocuments(),
                analysis.getTotalAssets(),
                analysis.getTotalPortfolioValue(),
                analysis.getErrorMessage(),
                analysis.getCreatedAt(),
                analysis.getStartedAt(),
                analysis.getCompletedAt()
        );
    }
}
