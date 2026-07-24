package com.portfoliointelligence.service;

import com.portfoliointelligence.dto.ClientResponse;
import com.portfoliointelligence.dto.CreateClientRequest;
import com.portfoliointelligence.entity.Client;
import com.portfoliointelligence.exception.ConflictException;
import com.portfoliointelligence.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void shouldCreateAndNormalizeClient() {
        given(clientRepository.existsByEmailIgnoreCase("client@example.com")).willReturn(false);
        given(clientRepository.existsByDocumentNumber("12345678901")).willReturn(false);
        given(clientRepository.save(any(Client.class))).willAnswer(invocation -> invocation.getArgument(0));

        ClientResponse response = clientService.create(new CreateClientRequest(
                "  Client   Example  ",
                " CLIENT@EXAMPLE.COM ",
                "123.456.789-01"
        ));

        assertThat(response.name()).isEqualTo("Client Example");
        assertThat(response.email()).isEqualTo("client@example.com");
        assertThat(response.documentNumber()).isEqualTo("12345678901");
    }

    @Test
    void shouldRejectDuplicatedEmail() {
        given(clientRepository.existsByEmailIgnoreCase("client@example.com")).willReturn(true);

        assertThatThrownBy(() -> clientService.create(new CreateClientRequest(
                "Client Example",
                "client@example.com",
                "12345678901"
        ))).isInstanceOf(ConflictException.class);
    }
}
