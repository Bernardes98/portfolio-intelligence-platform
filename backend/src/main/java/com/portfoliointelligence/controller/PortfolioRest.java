package com.portfoliointelligence.controller;

import com.portfoliointelligence.dto.InvestmentPositionResponse;
import com.portfoliointelligence.dto.PortfolioSummaryResponse;
import com.portfoliointelligence.service.InvestmentPositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/analyses/{analysisId}/portfolio"
)
@Tag(
        name = "Portfólio",
        description = """
                Consulta das posições extraídas e consolidação
                dos investimentos da análise.
                """
)
public class PortfolioRest {

    private final InvestmentPositionService positionService;

    public PortfolioRest(
            InvestmentPositionService positionService
    ) {
        this.positionService = positionService;
    }

    @GetMapping("/positions")
    @Operation(
            summary = "Listar posições extraídas",
            description = """
                    Lista todas as posições financeiras extraídas
                    dos relatórios da análise, sem consolidação.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Posições retornadas"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Análise não encontrada"
            )
    })
    public List<InvestmentPositionResponse> findPositions(
            @PathVariable UUID analysisId
    ) {
        return positionService.findPositions(
                analysisId
        );
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Consultar portfólio consolidado",
            description = """
                    Consolida as posições por identificador do ativo.
                    Quando o identificador não existe, utiliza o nome
                    normalizado do ativo.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Portfólio consolidado retornado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Análise não encontrada"
            )
    })
    public PortfolioSummaryResponse summarize(
            @PathVariable UUID analysisId
    ) {
        return positionService.summarize(
                analysisId
        );
    }
}