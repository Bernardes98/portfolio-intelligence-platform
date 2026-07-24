package com.portfoliointelligence.repository;

import com.portfoliointelligence.entity.InvestmentDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvestmentDocumentRepository
        extends JpaRepository<InvestmentDocument, UUID> {

    long countByAnalysisId(UUID analysisId);

    boolean existsByAnalysisIdAndChecksum(
            UUID analysisId,
            String checksum
    );

    List<InvestmentDocument>
    findAllByAnalysisIdOrderByCreatedAtAsc(UUID analysisId);
}