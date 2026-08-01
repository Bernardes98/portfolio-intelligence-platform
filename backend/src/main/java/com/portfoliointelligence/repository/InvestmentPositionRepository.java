package com.portfoliointelligence.repository;

import com.portfoliointelligence.entity.InvestmentPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InvestmentPositionRepository
        extends JpaRepository<InvestmentPosition, UUID> {

    List<InvestmentPosition>
    findAllByAnalysisIdOrderByAssetNameAsc(
            UUID analysisId
    );

    List<InvestmentPosition>
    findAllByDocumentIdOrderBySourceSequenceAsc(
            UUID documentId
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            DELETE FROM InvestmentPosition position
            WHERE position.documentId = :documentId
            """)
    void deleteAllByDocumentId(
            @Param("documentId") UUID documentId
    );
}