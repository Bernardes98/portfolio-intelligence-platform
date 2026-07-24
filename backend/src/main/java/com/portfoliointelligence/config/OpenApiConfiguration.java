package com.portfoliointelligence.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da documentação OpenAPI da aplicação.
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI portfolioIntelligenceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio Intelligence API")
                        .description("""
                                API responsável pelo cadastro de clientes,
                                criação de análises e processamento de
                                carteiras de investimentos.
                                """)
                        .version("v1")
                        .contact(new Contact()
                                .name("Felipe")
                                .email("felipe@email.com"))
                        .license(new License()
                                .name("Uso educacional e portfólio")));
    }
}