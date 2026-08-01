package com.portfoliointelligence.event;

import java.util.UUID;

public record AnalysisProcessingRequestedEvent(
        UUID analysisId
) {
}