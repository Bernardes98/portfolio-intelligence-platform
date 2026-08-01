package com.portfoliointelligence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Entity
@Table(name = "investment_positions")
public class InvestmentPosition {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(
            name = "analysis_id",
            nullable = false,
            updatable = false
    )
    private UUID analysisId;

    @Column(
            name = "document_id",
            nullable = false,
            updatable = false
    )
    private UUID documentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FinancialInstitution institution;

    @Column(
            name = "asset_name",
            nullable = false,
            length = 500
    )
    private String assetName;

    @Column(length = 150)
    private String identifier;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "asset_class",
            nullable = false,
            length = 40
    )
    private AssetClass assetClass;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "instrument_type",
            nullable = false,
            length = 50
    )
    private InstrumentType instrumentType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "investment_style",
            nullable = false,
            length = 50
    )
    private InvestmentStyle investmentStyle;

    @Column(precision = 24, scale = 8)
    private BigDecimal quantity;

    @Column(
            name = "unit_price_usd",
            precision = 19,
            scale = 6
    )
    private BigDecimal unitPriceUsd;

    @Column(
            name = "market_value_usd",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal marketValueUsd;

    @Column(
            name = "portfolio_percentage",
            precision = 12,
            scale = 6
    )
    private BigDecimal portfolioPercentage;

    @Column(name = "investment_date")
    private LocalDate investmentDate;

    @Column(
            name = "source_sequence",
            nullable = false
    )
    private int sourceSequence;

    @Column(
            name = "source_hash",
            nullable = false,
            length = 64
    )
    private String sourceHash;

    @Column(
            name = "source_line",
            nullable = false,
            length = 2000
    )
    private String sourceLine;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected InvestmentPosition() {
    }

    public InvestmentPosition(
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
            int sourceSequence,
            String sourceHash,
            String sourceLine
    ) {
        this.id = UUID.randomUUID();
        this.analysisId = analysisId;
        this.documentId = documentId;
        this.institution = institution;
        this.assetName = assetName;
        this.identifier = identifier;
        this.assetClass = assetClass;
        this.instrumentType = instrumentType;
        this.investmentStyle = investmentStyle;
        this.quantity = quantity;
        this.unitPriceUsd = unitPriceUsd;
        this.marketValueUsd = marketValueUsd;
        this.portfolioPercentage = portfolioPercentage;
        this.investmentDate = investmentDate;
        this.sourceSequence = sourceSequence;
        this.sourceHash = sourceHash;
        this.sourceLine = sourceLine;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}