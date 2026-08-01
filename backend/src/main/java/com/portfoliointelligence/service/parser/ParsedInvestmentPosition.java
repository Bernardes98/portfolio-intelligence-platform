package com.portfoliointelligence.service.parser;

import com.portfoliointelligence.entity.AssetClass;
import com.portfoliointelligence.entity.InstrumentType;
import com.portfoliointelligence.entity.InvestmentStyle;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedInvestmentPosition(

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
        int sourceSequence,
        String sourceHash,
        String sourceLine

) {
}