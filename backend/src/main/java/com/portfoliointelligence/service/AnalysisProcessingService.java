package com.portfoliointelligence.service;

import com.portfoliointelligence.dto.ProcessingResponse;
import com.portfoliointelligence.entity.DocumentStatus;
import com.portfoliointelligence.entity.InvestmentDocument;
import com.portfoliointelligence.entity.PortfolioAnalysis;
import com.portfoliointelligence.event.AnalysisProcessingRequestedEvent;
import com.portfoliointelligence.exception.InvalidAnalysisStatusException;
import com.portfoliointelligence.exception.ResourceNotFoundException;
import com.portfoliointelligence.repository.InvestmentDocumentRepository;
import com.portfoliointelligence.repository.PortfolioAnalysisRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AnalysisProcessingService {

    private final PortfolioAnalysisRepository analysisRepository;
    private final InvestmentDocumentRepository documentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AnalysisProcessingService(
            PortfolioAnalysisRepository analysisRepository,
            InvestmentDocumentRepository documentRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.analysisRepository = analysisRepository;
        this.documentRepository = documentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ProcessingResponse queue(UUID analysisId) {
        PortfolioAnalysis analysis =
                analysisRepository
                        .findByIdForUpdate(analysisId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Análise não encontrada: "
                                                + analysisId
                                )
                        );

        if (!analysis.canQueueProcessing()) {
            throw new InvalidAnalysisStatusException(
                    "A análise não pode ser processada "
                            + "quando está com status "
                            + analysis.getStatus()
                            + "."
            );
        }

        List<InvestmentDocument> documents =
                documentRepository
                        .findAllByAnalysisIdAndStatusInOrderByCreatedAtAsc(
                                analysisId,
                                List.of(
                                        DocumentStatus.UPLOADED,
                                        DocumentStatus.FAILED
                                )
                        );

        if (documents.isEmpty()) {
            throw new InvalidAnalysisStatusException(
                    "Não existem documentos pendentes "
                            + "para processamento."
            );
        }

        documents.forEach(InvestmentDocument::queue);
        analysis.queueProcessing();

        eventPublisher.publishEvent(
                new AnalysisProcessingRequestedEvent(
                        analysisId
                )
        );

        return new ProcessingResponse(
                analysisId,
                analysis.getStatus(),
                documents.size(),
                "A análise foi adicionada à fila de processamento."
        );
    }
}