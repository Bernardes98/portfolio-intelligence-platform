package com.portfoliointelligence.dto;

import com.portfoliointelligence.entity.AssetClass;
import com.portfoliointelligence.entity.FinancialInstitution;
import com.portfoliointelligence.entity.InstrumentType;
import com.portfoliointelligence.entity.InvestmentStyle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ConsolidatedInvestmentResponse(

        String clusterKey,
        String assetName,
        String identifier,
        AssetClass assetClass,
        InstrumentType instrumentType,
        InvestmentStyle investmentStyle,
        BigDecimal quantity,
        BigDecimal marketValueUsd,
        BigDecimal portfolioPercentage,
        LocalDate earliestInvestmentDate,
        List<FinancialInstitution> institutions,
        int sourcePositions

) {
}