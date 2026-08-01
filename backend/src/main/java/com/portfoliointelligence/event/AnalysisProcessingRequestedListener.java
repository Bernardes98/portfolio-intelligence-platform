package com.portfoliointelligence.event;

import com.portfoliointelligence.service.DocumentProcessingWorker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AnalysisProcessingRequestedListener {

    private final DocumentProcessingWorker processingWorker;

    public AnalysisProcessingRequestedListener(
            DocumentProcessingWorker processingWorker
    ) {
        this.processingWorker = processingWorker;
    }

    @Async("documentProcessingExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            AnalysisProcessingRequestedEvent event
    ) {
        processingWorker.processAnalysis(
                event.analysisId()
        );
    }
}