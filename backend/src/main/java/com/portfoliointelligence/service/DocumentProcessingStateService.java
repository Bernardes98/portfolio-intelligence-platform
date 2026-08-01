package com.portfoliointelligence.service;

import com.portfoliointelligence.entity.DocumentExtraction;
import com.portfoliointelligence.entity.DocumentStatus;
import com.portfoliointelligence.entity.InvestmentDocument;
import com.portfoliointelligence.entity.InvestmentPosition;
import com.portfoliointelligence.entity.PortfolioAnalysis;
import com.portfoliointelligence.exception.ResourceNotFoundException;
import com.portfoliointelligence.repository.DocumentExtractionRepository;
import com.portfoliointelligence.repository.InvestmentDocumentRepository;
import com.portfoliointelligence.repository.InvestmentPositionRepository;
import com.portfoliointelligence.repository.PortfolioAnalysisRepository;
import com.portfoliointelligence.service.parser.ParsedInvestmentPosition;
import com.portfoliointelligence.service.parser.StatementParsingResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentProcessingStateService {

    private final PortfolioAnalysisRepository analysisRepository;
    private final InvestmentDocumentRepository documentRepository;
    private final DocumentExtractionRepository extractionRepository;
    private final InvestmentPositionRepository positionRepository;

    public DocumentProcessingStateService(
            PortfolioAnalysisRepository analysisRepository,
            InvestmentDocumentRepository documentRepository,
            DocumentExtractionRepository extractionRepository,
            InvestmentPositionRepository positionRepository
    ) {
        this.analysisRepository = analysisRepository;
        this.documentRepository = documentRepository;
        this.extractionRepository = extractionRepository;
        this.positionRepository = positionRepository;
    }

    @Transactional
    public void startAnalysis(UUID analysisId) {
        PortfolioAnalysis analysis =
                getAnalysisForUpdate(analysisId);

        analysis.startProcessing();
    }

    @Transactional(readOnly = true)
    public List<UUID> findQueuedDocumentIds(
            UUID analysisId
    ) {
        return documentRepository
                .findAllByAnalysisIdAndStatusOrderByCreatedAtAsc(
                        analysisId,
                        DocumentStatus.QUEUED
                )
                .stream()
                .map(InvestmentDocument::getId)
                .toList();
    }

    @Transactional
    public DocumentWorkItem startDocument(
            UUID documentId
    ) {
        InvestmentDocument document =
                getDocument(documentId);

        document.startProcessing();

        return new DocumentWorkItem(
                document.getId(),
                document.getAnalysisId(),
                document.getStoragePath()
        );
    }

    @Transactional
    public void completeDocument(
            UUID documentId,
            PdfTextExtractionService.PdfExtractionResult extractionResult,
            StatementParsingResult parsingResult
    ) {
        InvestmentDocument document =
                getDocument(documentId);

        DocumentExtraction extraction =
                extractionRepository
                        .findById(documentId)
                        .orElseGet(() ->
                                new DocumentExtraction(
                                        documentId
                                )
                        );

        extraction.update(
                extractionResult.institution(),
                extractionResult.pageCount(),
                extractionResult.extractedText(),
                parsingResult.positions().size(),
                parsingResult.warnings()
        );

        extractionRepository.save(extraction);

        positionRepository.deleteAllByDocumentId(
                documentId
        );

        List<InvestmentPosition> positions =
                parsingResult
                        .positions()
                        .stream()
                        .map(parsed ->
                                createPosition(
                                        document,
                                        extractionResult,
                                        parsed
                                )
                        )
                        .toList();

        positionRepository.saveAll(positions);

        document.markProcessed();
    }

    @Transactional
    public void failDocument(
            UUID documentId,
            String errorMessage
    ) {
        InvestmentDocument document =
                getDocument(documentId);

        document.markFailed(errorMessage);
    }

    @Transactional
    public void finishAnalysis(UUID analysisId) {
        PortfolioAnalysis analysis =
                getAnalysisForUpdate(analysisId);

        long failedDocuments =
                documentRepository
                        .countByAnalysisIdAndStatus(
                                analysisId,
                                DocumentStatus.FAILED
                        );

        long queuedDocuments =
                documentRepository
                        .countByAnalysisIdAndStatus(
                                analysisId,
                                DocumentStatus.QUEUED
                        );

        long processingDocuments =
                documentRepository
                        .countByAnalysisIdAndStatus(
                                analysisId,
                                DocumentStatus.PROCESSING
                        );

        if (failedDocuments > 0) {
            analysis.failProcessing(
                    failedDocuments
                            + " documento(s) falharam "
                            + "durante o processamento."
            );
            return;
        }

        if (queuedDocuments > 0
                || processingDocuments > 0) {
            analysis.failProcessing(
                    "O processamento terminou com "
                            + "documentos pendentes."
            );
            return;
        }

        analysis.completeProcessing();
    }

    @Transactional
    public void failAnalysis(
            UUID analysisId,
            String errorMessage
    ) {
        PortfolioAnalysis analysis =
                getAnalysisForUpdate(analysisId);

        analysis.failProcessing(errorMessage);
    }

    private InvestmentPosition createPosition(
            InvestmentDocument document,
            PdfTextExtractionService.PdfExtractionResult extraction,
            ParsedInvestmentPosition parsed
    ) {
        return new InvestmentPosition(
                document.getAnalysisId(),
                document.getId(),
                extraction.institution(),
                parsed.assetName(),
                parsed.identifier(),
                parsed.assetClass(),
                parsed.instrumentType(),
                parsed.investmentStyle(),
                parsed.quantity(),
                parsed.unitPriceUsd(),
                parsed.marketValueUsd(),
                parsed.portfolioPercentage(),
                parsed.investmentDate(),
                parsed.sourceSequence(),
                parsed.sourceHash(),
                parsed.sourceLine()
        );
    }

    private PortfolioAnalysis getAnalysisForUpdate(
            UUID analysisId
    ) {
        return analysisRepository
                .findByIdForUpdate(analysisId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Análise não encontrada: "
                                        + analysisId
                        )
                );
    }

    private InvestmentDocument getDocument(
            UUID documentId
    ) {
        return documentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Documento não encontrado: "
                                        + documentId
                        )
                );
    }

    public record DocumentWorkItem(
            UUID documentId,
            UUID analysisId,
            String storagePath
    ) {
    }
}