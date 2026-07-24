package com.portfoliointelligence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PortfolioIntelligenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                PortfolioIntelligenceApplication.class,
                args
        );
    }
}