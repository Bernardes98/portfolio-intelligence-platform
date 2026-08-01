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
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "document_extractions")
public class DocumentExtraction {

    @Id
    @Column(
            name = "document_id",
            nullable = false,
            updatable = false
    )
    private UUID documentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FinancialInstitution institution;

    @Column(name = "page_count", nullable = false)
    private int pageCount;

    @Column(
            name = "extracted_text",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String extractedText;

    @Column(name = "character_count", nullable = false)
    private int characterCount;

    @Column(
            name = "position_count",
            nullable = false
    )
    private int positionCount;

    @Column(
            name = "parsing_warnings",
            columnDefinition = "TEXT"
    )
    private String parsingWarnings;

    @Column(
            name = "extracted_at",
            nullable = false
    )
    private Instant extractedAt;

    @Column(name = "parsed_at")
    private Instant parsedAt;

    protected DocumentExtraction() {
    }

    public DocumentExtraction(UUID documentId) {
        this.documentId = documentId;
    }

    @PrePersist
    void onCreate() {
        if (extractedAt == null) {
            extractedAt = Instant.now();
        }
    }

    public void update(
            FinancialInstitution institution,
            int pageCount,
            String extractedText,
            int positionCount,
            List<String> warnings
    ) {
        this.institution = institution;
        this.pageCount = pageCount;
        this.extractedText = extractedText;
        this.characterCount = extractedText.length();
        this.positionCount = positionCount;
        this.parsingWarnings = warnings == null
                || warnings.isEmpty()
                ? null
                : String.join("\n", warnings);
        this.extractedAt = Instant.now();
        this.parsedAt = Instant.now();
    }
}