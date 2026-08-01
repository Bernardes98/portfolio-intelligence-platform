package com.portfoliointelligence.dto;

import com.portfoliointelligence.entity.AssetClass;

import java.math.BigDecimal;

public record AssetClassSummaryResponse(

        AssetClass assetClass,
        int assets,
        BigDecimal marketValueUsd,
        BigDecimal portfolioPercentage

) {
}