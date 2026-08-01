package com.portfoliointelligence.repository;

import com.portfoliointelligence.entity.DocumentStatus;
import com.portfoliointelligence.entity.InvestmentDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentDocumentRepository
        extends JpaRepository<InvestmentDocument, UUID> {

    long countByAnalysisId(UUID analysisId);

    long countByAnalysisIdAndStatus(
            UUID analysisId,
            DocumentStatus status
    );

    boolean existsByAnalysisIdAndChecksum(
            UUID analysisId,
            String checksum
    );

    Optional<InvestmentDocument> findByIdAndAnalysisId(
            UUID id,
            UUID analysisId
    );

    List<InvestmentDocument>
    findAllByAnalysisIdOrderByCreatedAtAsc(
            UUID analysisId
    );

    List<InvestmentDocument>
    findAllByAnalysisIdAndStatusOrderByCreatedAtAsc(
            UUID analysisId,
            DocumentStatus status
    );

    List<InvestmentDocument>
    findAllByAnalysisIdAndStatusInOrderByCreatedAtAsc(
            UUID analysisId,
            Collection<DocumentStatus> statuses
    );
}