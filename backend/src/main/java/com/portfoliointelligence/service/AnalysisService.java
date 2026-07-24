package com.portfoliointelligence.service;

import com.portfoliointelligence.dto.AnalysisResponse;
import com.portfoliointelligence.entity.PortfolioAnalysis;
import com.portfoliointelligence.exception.ResourceNotFoundException;
import com.portfoliointelligence.repository.PortfolioAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AnalysisService {

    private final PortfolioAnalysisRepository analysisRepository;
    private final ClientService clientService;

    public AnalysisService(
            PortfolioAnalysisRepository analysisRepository,
            ClientService clientService
    ) {
        this.analysisRepository = analysisRepository;
        this.clientService = clientService;
    }

    @Transactional
    public AnalysisResponse create(UUID clientId) {
        clientService.getEntity(clientId);
        PortfolioAnalysis analysis = new PortfolioAnalysis(clientId);
        return AnalysisResponse.from(analysisRepository.save(analysis));
    }

    @Transactional(readOnly = true)
    public AnalysisResponse findById(UUID analysisId) {
        return AnalysisResponse.from(getEntity(analysisId));
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponse> findByClient(UUID clientId) {
        clientService.getEntity(clientId);
        return analysisRepository.findAllByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(AnalysisResponse::from)
                .toList();
    }

    private PortfolioAnalysis getEntity(UUID analysisId) {
        return analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Portfolio analysis not found with id: " + analysisId));
    }
}
