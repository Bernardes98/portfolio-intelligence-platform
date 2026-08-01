package com.portfoliointelligence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "investment_documents")
public class InvestmentDocument {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "analysis_id", nullable = false, updatable = false)
    private UUID analysisId;

    @Column(
            name = "original_filename",
            nullable = false,
            length = 255
    )
    private String originalFilename;

    @Column(
            name = "storage_filename",
            nullable = false,
            length = 255
    )
    private String storageFilename;

    @Column(
            name = "storage_path",
            nullable = false,
            length = 1000
    )
    private String storagePath;

    @Column(
            name = "content_type",
            nullable = false,
            length = 100
    )
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(nullable = false, length = 64)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(
            name = "processing_attempts",
            nullable = false
    )
    private int processingAttempts;

    protected InvestmentDocument() {
    }

    public InvestmentDocument(
            UUID id,
            UUID analysisId,
            String originalFilename,
            String storageFilename,
            String storagePath,
            String contentType,
            long fileSize,
            String checksum
    ) {
        this.id = id;
        this.analysisId = analysisId;
        this.originalFilename = originalFilename;
        this.storageFilename = storageFilename;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.status = DocumentStatus.UPLOADED;
        this.processingAttempts = 0;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean canBeQueued() {
        return status == DocumentStatus.UPLOADED
                || status == DocumentStatus.FAILED;
    }

    public void queue() {
        if (!canBeQueued()) {
            throw new IllegalStateException(
                    "O documento não pode ser colocado na fila "
                            + "quando está com status " + status + "."
            );
        }

        this.status = DocumentStatus.QUEUED;
        this.errorMessage = null;
        this.processingStartedAt = null;
        this.processedAt = null;
    }

    public void startProcessing() {
        if (status != DocumentStatus.QUEUED) {
            throw new IllegalStateException(
                    "Somente documentos na fila podem ser processados."
            );
        }

        this.status = DocumentStatus.PROCESSING;
        this.processingStartedAt = Instant.now();
        this.processingAttempts++;
        this.errorMessage = null;
    }

    public void markProcessed() {
        if (status != DocumentStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Somente documentos em processamento "
                            + "podem ser concluídos."
            );
        }

        this.status = DocumentStatus.PROCESSED;
        this.processedAt = Instant.now();
        this.errorMessage = null;
    }

    public void markFailed(String message) {
        this.status = DocumentStatus.FAILED;
        this.processedAt = Instant.now();
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