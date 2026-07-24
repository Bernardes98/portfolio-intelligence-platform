package com.portfoliointelligence.controller;

import com.portfoliointelligence.dto.ClientResponse;
import com.portfoliointelligence.dto.CreateClientRequest;
import com.portfoliointelligence.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/clients")
@Tag(
        name = "Clientes",
        description = "Operações relacionadas aos clientes investidores"
)
public class ClientRest {

    private final ClientService clientService;

    public ClientRest(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar cliente",
            description = "Cadastra um novo cliente investidor."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados da requisição inválidos"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail ou documento já cadastrado"
            )
    })

    public ClientResponse create(@Valid @RequestBody CreateClientRequest request) {
        return clientService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar cliente",
            description = "Consulta um cliente pelo identificador UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente localizado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado"
            )
    })
    public ClientResponse findById(@PathVariable UUID id) {
        return clientService.findById(id);
    }
}
