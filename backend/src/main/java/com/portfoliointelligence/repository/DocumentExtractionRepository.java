package com.portfoliointelligence.repository;

import com.portfoliointelligence.entity.DocumentExtraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentExtractionRepository
        extends JpaRepository<DocumentExtraction, UUID> {
}