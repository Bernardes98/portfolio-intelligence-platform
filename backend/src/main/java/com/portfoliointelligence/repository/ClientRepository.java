package com.portfoliointelligence.repository;

import com.portfoliointelligence.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDocumentNumber(String documentNumber);
}
