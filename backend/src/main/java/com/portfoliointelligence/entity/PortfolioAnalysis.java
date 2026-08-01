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
import java.util.UUID;

@Getter
@Entity
@Table(name = "portfolio_analyses")
public class PortfolioAnalysis {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

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

    @Column(
            name = "total_portfolio_value",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal totalPortfolioValue;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
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
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean acceptsDocumentUpload() {
        return status == AnalysisStatus.CREATED
                || status == AnalysisStatus.DOCUMENTS_UPLOADED;
    }

    public void registerUploadedDocuments(
            int uploadedDocuments,
            int maxDocuments
    ) {
        if (!acceptsDocumentUpload()) {
            throw new IllegalStateException(
                    "A análise não aceita novos documentos."
            );
        }

        if (uploadedDocuments <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade de documentos deve ser maior que zero."
            );
        }

        int updatedTotal = totalDocuments + uploadedDocuments;

        if (updatedTotal > maxDocuments) {
            throw new IllegalStateException(
                    "O limite máximo de documentos foi excedido."
            );
        }

        this.totalDocuments = updatedTotal;
        this.status = AnalysisStatus.DOCUMENTS_UPLOADED;
        this.errorMessage = null;
    }

    public boolean canQueueProcessing() {
        return status == AnalysisStatus.DOCUMENTS_UPLOADED
                || status == AnalysisStatus.FAILED;
    }

    public void queueProcessing() {
        if (!canQueueProcessing()) {
            throw new IllegalStateException(
                    "A análise não pode ser processada "
                            + "quando está com status " + status + "."
            );
        }

        this.status = AnalysisStatus.QUEUED;
        this.startedAt = null;
        this.completedAt = null;
        this.errorMessage = null;
    }

    public void startProcessing() {
        if (status != AnalysisStatus.QUEUED) {
            throw new IllegalStateException(
                    "Somente análises na fila podem iniciar "
                            + "o processamento."
            );
        }

        this.status = AnalysisStatus.PROCESSING;
        this.startedAt = Instant.now();
        this.completedAt = null;
        this.errorMessage = null;
    }

    public void completeProcessing() {
        if (status != AnalysisStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Somente análises em processamento "
                            + "podem ser concluídas."
            );
        }

        this.status = AnalysisStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.errorMessage = null;
    }

    public void failProcessing(String message) {
        this.status = AnalysisStatus.FAILED;
        this.completedAt = Instant.now();
        this.errorMessage = truncate(message);
    }

    private String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "Erro desconhecido durante o processamento.";
        }

        if (message.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return message;
        }

        return message.substring(
                0,
                MAX_ERROR_MESSAGE_LENGTH
        );
    }
}