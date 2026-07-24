package com.portfoliointelligence;

import com.portfoliointelligence.dto.AnalysisResponse;
import com.portfoliointelligence.dto.ClientResponse;
import com.portfoliointelligence.dto.CreateClientRequest;
import com.portfoliointelligence.entity.AnalysisStatus;
import com.portfoliointelligence.service.AnalysisService;
import com.portfoliointelligence.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class ClientAnalysisIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private ClientService clientService;

    @Autowired
    private AnalysisService analysisService;

    @Test
    void shouldPersistClientAndCreateAnalysis() {
        ClientResponse client = clientService.create(new CreateClientRequest(
                "Integration Client",
                "integration@example.com",
                "99887766554"
        ));

        AnalysisResponse analysis = analysisService.create(client.id());

        assertThat(analysis.id()).isNotNull();
        assertThat(analysis.clientId()).isEqualTo(client.id());
        assertThat(analysis.status()).isEqualTo(AnalysisStatus.CREATED);
        assertThat(analysisService.findById(analysis.id()).id()).isEqualTo(analysis.id());
    }
}
