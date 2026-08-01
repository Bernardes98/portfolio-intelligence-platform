package com.portfoliointelligence.service;

import com.portfoliointelligence.service.parser.InvestmentParserService;
import com.portfoliointelligence.service.parser.StatementParsingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentProcessingWorker {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    DocumentProcessingWorker.class
            );

    private final DocumentProcessingStateService stateService;
    private final PdfTextExtractionService extractionService;
    private final InvestmentParserService parserService;

    public DocumentProcessingWorker(
            DocumentProcessingStateService stateService,
            PdfTextExtractionService extractionService,
            InvestmentParserService parserService
    ) {
        this.stateService = stateService;
        this.extractionService = extractionService;
        this.parserService = parserService;
    }

    public void processAnalysis(UUID analysisId) {
        try {
            stateService.startAnalysis(analysisId);

            List<UUID> documentIds =
                    stateService.findQueuedDocumentIds(
                            analysisId
                    );

            for (UUID documentId : documentIds) {
                processDocument(documentId);
            }

            stateService.finishAnalysis(analysisId);
        } catch (Exception exception) {
            LOGGER.error(
                    "Erro ao processar a análise {}.",
                    analysisId,
                    exception
            );

            try {
                stateService.failAnalysis(
                        analysisId,
                        safeMessage(exception)
                );
            } catch (Exception stateException) {
                LOGGER.error(
                        "Não foi possível marcar a análise {} "
                                + "como falha.",
                        analysisId,
                        stateException
                );
            }
        }
    }

    private void processDocument(UUID documentId) {
        try {
            DocumentProcessingStateService.DocumentWorkItem workItem =
                    stateService.startDocument(documentId);

            PdfTextExtractionService.PdfExtractionResult extraction =
                    extractionService.extract(
                            workItem.storagePath()
                    );

            StatementParsingResult parsing =
                    parserService.parse(
                            extraction.institution(),
                            extraction.extractedText()
                    );

            stateService.completeDocument(
                    documentId,
                    extraction,
                    parsing
            );
        } catch (Exception exception) {
            LOGGER.error(
                    "Erro ao processar o documento {}.",
                    documentId,
                    exception
            );

            stateService.failDocument(
                    documentId,
                    safeMessage(exception)
            );
        }
    }

    private String safeMessage(Exception exception) {
        if (exception.getMessage() == null
                || exception.getMessage().isBlank()) {
            return "Erro inesperado durante o processamento.";
        }

        return exception.getMessage();
    }
}