package com.portfoliointelligence.dto;

import com.portfoliointelligence.entity.Client;

import java.time.Instant;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,
        String email,
        String documentNumber,
        Instant createdAt,
        Instant updatedAt
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getDocumentNumber(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}
