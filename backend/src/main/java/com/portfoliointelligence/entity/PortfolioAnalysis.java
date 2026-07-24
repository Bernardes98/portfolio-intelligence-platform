package com.portfoliointelligence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portfolio_analyses")
public class PortfolioAnalysis {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "client_id", nullable = false, updatable = false)
    private UUID clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AnalysisStatus status;

    @Column(name = "total_documents", nullable = false)
    private int totalDocuments;

    @Column(name = "total_assets", nullable = false)
    private int totalAssets;

    @Column(name = "total_portfolio_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalPortfolioValue;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected PortfolioAnalysis() {
    }

    public PortfolioAnalysis(UUID clientId) {
        this.id = UUID.randomUUID();
        this.clientId = clientId;
        this.status = AnalysisStatus.CREATED;
        this.totalDocuments = 0;
        this.totalAssets = 0;
        this.totalPortfolioValue = BigDecimal.ZERO;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public int getTotalDocuments() {
        return totalDocuments;
    }

    public int getTotalAssets() {
        return totalAssets;
    }

    public BigDecimal getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
