package com.k8slearn.orders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
public class OrdersController {

    private final RestClient restClient;
    private final String catalogUrl;

    public OrdersController(@Value("${catalog.url:http://localhost:8080}") String catalogUrl) {
        this.catalogUrl = catalogUrl;
        this.restClient = RestClient.builder().baseUrl(catalogUrl).build();
    }

    // Test locally: curl http://localhost:8083/
    @GetMapping("/")
    public Map<String, String> getStatus() {
        return Map.of(
            "service", "orders",
            "status", "ok"
        );
    }

    // Inter-service call: curl http://localhost:8083/orders (or http://orders:8083/orders in cluster)
    @GetMapping("/orders")
    public Map<String, Object> getOrders() {
        List<Map<String, Object>> products;
        try {
            products = restClient.get()
                    .uri("/products")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (Exception ex) {
            return Map.of(
                "orderId", "ORD-1001",
                "service", "orders",
                "status", "DEGRADED",
                "catalogUrl", catalogUrl,
                "error", ex.getMessage() != null ? ex.getMessage() : ex.toString(),
                "items", List.of()
            );
        }

        return Map.of(
            "orderId", "ORD-1001",
            "customer", "Learner",
            "service", "orders",
            "status", "CONFIRMED",
            "timestamp", Instant.now().toString(),
            "catalogSource", catalogUrl + "/products",
            "itemCount", products != null ? products.size() : 0,
            "items", products != null ? products : List.of()
        );
    }
}
