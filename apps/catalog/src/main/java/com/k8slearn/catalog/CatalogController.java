package com.k8slearn.catalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CatalogController {

    private final ProductRepository productRepository;

    @Value("${catalog.welcome-message}")
    private String welcomeMessage;

    public CatalogController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Test locally: curl http://localhost:8080/
    @GetMapping("/")
    public Map<String, Object> getStatus() {
        return Map.of(
            "service", "catalog",
            "status", "ok",
            "welcomeMessage", welcomeMessage
        );
    }

    // Test in-cluster: curl http://catalog:8080/products
    @GetMapping("/products")
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }
}
