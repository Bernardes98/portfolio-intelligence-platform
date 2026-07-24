package com.portfoliointelligence.service;

import com.portfoliointelligence.dto.ClientResponse;
import com.portfoliointelligence.dto.CreateClientRequest;
import com.portfoliointelligence.entity.Client;
import com.portfoliointelligence.exception.ConflictException;
import com.portfoliointelligence.exception.ResourceNotFoundException;
import com.portfoliointelligence.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public ClientResponse create(CreateClientRequest request) {
        String name = normalizeName(request.name());
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String documentNumber = request.documentNumber().replaceAll("\\D", "");

        if (clientRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A client with this email already exists.");
        }

        if (clientRepository.existsByDocumentNumber(documentNumber)) {
            throw new ConflictException("A client with this document number already exists.");
        }

        Client client = new Client(name, email, documentNumber);
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(UUID id) {
        return ClientResponse.from(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Client getEntity(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id: " + id));
    }

    private static String normalizeName(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }
}
