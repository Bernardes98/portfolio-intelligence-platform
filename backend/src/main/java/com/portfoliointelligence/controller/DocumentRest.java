package com.portfoliointelligence.controller;

import com.portfoliointelligence.dto.DocumentResponse;
import com.portfoliointelligence.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/analyses/{analysisId}/documents"
)
@Tag(
        name = "Documentos",
        description = "Upload e consulta de relatórios PDF"
)
public class DocumentRest {

    private final DocumentService documentService;

    public DocumentRest(
            DocumentService documentService
    ) {
        this.documentService = documentService;
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Enviar relatórios PDF",
            description = """
                    Envia de um a seis relatórios PDF para uma
                    análise de carteira.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Documentos enviados com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Documento inválido"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Análise não encontrada"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Documento duplicado"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Limite ou status inválido"
            )
    })
    public List<DocumentResponse> upload(
            @PathVariable
            UUID analysisId,

            @Parameter(
                    description = "Arquivos PDF da análise",
                    required = true
            )
            @RequestPart("files")
            List<MultipartFile> files
    ) {
        return documentService.upload(
                analysisId,
                files
        );
    }

    @GetMapping
    @Operation(
            summary = "Listar documentos",
            description = """
                    Lista os documentos enviados para uma análise.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Documentos retornados"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Análise não encontrada"
            )
    })
    public List<DocumentResponse> findByAnalysis(
            @PathVariable UUID analysisId
    ) {
        return documentService.findByAnalysis(
                analysisId
        );
    }
}