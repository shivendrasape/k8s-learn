package com.k8slearn.catalog;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class CatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(ProductRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                    new Product("Kubernetes in Action", new BigDecimal("49.99"), "Comprehensive guide to Kubernetes architecture and primitives"),
                    new Product("Cloud Native Patterns", new BigDecimal("39.99"), "Designing resilient microservices in modern cloud infrastructure")
                ));
            }
        };
    }
}
