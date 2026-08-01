package com.portfoliointelligence.controller;

import com.portfoliointelligence.dto.ProcessingResponse;
import com.portfoliointelligence.service.AnalysisProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/analyses/{analysisId}/processing"
)
@Tag(
        name = "Processamento",
        description = "Processamento assíncrono dos relatórios"
)
public class ProcessingRest {

    private final AnalysisProcessingService processingService;

    public ProcessingRest(
            AnalysisProcessingService processingService
    ) {
        this.processingService = processingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Iniciar processamento",
            description = """
                    Adiciona os documentos pendentes da análise
                    à fila de processamento assíncrono.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Processamento iniciado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Análise não encontrada"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Análise sem documentos ou "
                            + "com status inválido"
            )
    })
    public ProcessingResponse process(
            @PathVariable UUID analysisId
    ) {
        return processingService.queue(analysisId);
    }
}