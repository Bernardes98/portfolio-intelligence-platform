package com.portfoliointelligence.service;

import com.portfoliointelligence.dto.DocumentExtractionResponse;
import com.portfoliointelligence.entity.DocumentExtraction;
import com.portfoliointelligence.exception.ResourceNotFoundException;
import com.portfoliointelligence.repository.DocumentExtractionRepository;
import com.portfoliointelligence.repository.InvestmentDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DocumentExtractionService {

    private final InvestmentDocumentRepository documentRepository;
    private final DocumentExtractionRepository extractionRepository;

    public DocumentExtractionService(
            InvestmentDocumentRepository documentRepository,
            DocumentExtractionRepository extractionRepository
    ) {
        this.documentRepository = documentRepository;
        this.extractionRepository = extractionRepository;
    }

    @Transactional(readOnly = true)
    public DocumentExtractionResponse find(
            UUID analysisId,
            UUID documentId
    ) {
        documentRepository
                .findByIdAndAnalysisId(
                        documentId,
                        analysisId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Documento não encontrado na análise."
                        )
                );

        DocumentExtraction extraction =
                extractionRepository
                        .findById(documentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "A extração do documento "
                                                + "ainda não está disponível."
                                )
                        );

        return DocumentExtractionResponse.from(extraction);
    }
}