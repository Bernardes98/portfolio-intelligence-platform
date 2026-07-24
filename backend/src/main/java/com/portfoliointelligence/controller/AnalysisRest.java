package com.portfoliointelligence.controller;

import com.portfoliointelligence.dto.AnalysisResponse;
import com.portfoliointelligence.service.AnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(
        name = "Análises",
        description = "Criação e consulta de análises de carteiras"
)
public class AnalysisRest {

    private final AnalysisService analysisService;

    public AnalysisRest(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/clients/{clientId}/analyses")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar análise",
            description = "Cria uma nova análise de carteira para o cliente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Análise criada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado"
            )
    })

    public AnalysisResponse create(@PathVariable UUID clientId) {
        return analysisService.create(clientId);
    }

    @GetMapping("/analyses/{analysisId}")
    @Operation(
            summary = "Consultar análise",
            description = "Consulta uma análise pelo identificador UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Análise localizada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Análise não encontrada"
            )
    })

    public AnalysisResponse findById(@PathVariable UUID analysisId) {
        return analysisService.findById(analysisId);
    }

    @GetMapping("/clients/{clientId}/analyses")
    @Operation(
            summary = "Listar análises do cliente",
            description = "Lista todas as análises pertencentes ao cliente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Análises retornadas com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado"
            )
    })

    public List<AnalysisResponse> findByClient(@PathVariable UUID clientId) {
        return analysisService.findByClient(clientId);
    }
}
