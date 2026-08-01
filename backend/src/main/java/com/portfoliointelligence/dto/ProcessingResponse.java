package com.portfoliointelligence.dto;

import com.portfoliointelligence.entity.AnalysisStatus;

import java.util.UUID;

public record ProcessingResponse(

        UUID analysisId,
        AnalysisStatus status,
        int queuedDocuments,
        String message

) {
}