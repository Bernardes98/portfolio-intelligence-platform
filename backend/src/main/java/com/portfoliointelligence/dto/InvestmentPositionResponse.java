package com.portfoliointelligence.dto;

import com.portfoliointelligence.entity.AssetClass;
import com.portfoliointelligence.entity.FinancialInstitution;
import com.portfoliointelligence.entity.InstrumentType;
import com.portfoliointelligence.entity.InvestmentPosition;
import com.portfoliointelligence.entity.InvestmentStyle;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvestmentPositionResponse(

        UUID id,
        UUID analysisId,
        UUID documentId,
        FinancialInstitution institution,
        String assetName,
        String identifier,
        AssetClass assetClass,
        InstrumentType instrumentType,
        InvestmentStyle investmentStyle,
        BigDecimal quantity,
        BigDecimal unitPriceUsd,
        BigDecimal marketValueUsd,
        BigDecimal portfolioPercentage,
        LocalDate investmentDate,
        Instant createdAt

) {

    public static InvestmentPositionResponse from(
            InvestmentPosition position
    ) {
        return new InvestmentPositionResponse(
                position.getId(),
                position.getAnalysisId(),
                position.getDocumentId(),
                position.getInstitution(),
                position.getAssetName(),
                position.getIdentifier(),
                position.getAssetClass(),
                position.getInstrumentType(),
                position.getInvestmentStyle(),
                position.getQuantity(),
                position.getUnitPriceUsd(),
                position.getMarketValueUsd(),
                position.getPortfolioPercentage(),
                position.getInvestmentDate(),
                position.getCreatedAt()
        );
    }
}