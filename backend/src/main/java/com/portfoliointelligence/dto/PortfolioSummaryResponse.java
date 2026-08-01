package com.portfoliointelligence.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PortfolioSummaryResponse(

        UUID analysisId,
        int sourcePositions,
        int consolidatedAssets,
        BigDecimal totalMarketValueUsd,
        List<AssetClassSummaryResponse> byAssetClass,
        List<ConsolidatedInvestmentResponse> assets

) {
}