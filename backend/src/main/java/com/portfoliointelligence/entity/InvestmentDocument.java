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

    @Column(name = "processed_at")
    private Instant processedAt;

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
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}