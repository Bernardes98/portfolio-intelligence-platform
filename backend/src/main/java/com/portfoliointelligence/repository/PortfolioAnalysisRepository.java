package com.portfoliointelligence.repository;

import com.portfoliointelligence.entity.PortfolioAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PortfolioAnalysisRepository extends JpaRepository<PortfolioAnalysis, UUID> {

    List<PortfolioAnalysis> findAllByClientIdOrderByCreatedAtDesc(UUID clientId);
}
