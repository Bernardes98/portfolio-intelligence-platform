package com.portfoliointelligence.repository;

import com.portfoliointelligence.entity.PortfolioAnalysis;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioAnalysisRepository
        extends JpaRepository<PortfolioAnalysis, UUID> {

    List<PortfolioAnalysis>
    findAllByClientIdOrderByCreatedAtDesc(UUID clientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT analysis
            FROM PortfolioAnalysis analysis
            WHERE analysis.id = :analysisId
            """)
    Optional<PortfolioAnalysis> findByIdForUpdate(
            @Param("analysisId") UUID analysisId
    );
}