package com.k8slearn.catalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Value("${catalog.welcome-message}")
    private String welcomeMessage;

    public CatalogController(ProductRepository productRepository, ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
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

    // Operational resilience simulation: Force readiness failure for Playbook 06
    // Marks ReadinessState as REFUSING_TRAFFIC. Spring Boot sets /actuator/health/readiness to OUT_OF_SERVICE (503).
    // Kubelet will remove this pod from the Service endpoint slice without terminating the pod container.
    @PostMapping("/actuator/readiness/refuse")
    public Map<String, String> refuseTraffic() {
        AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.REFUSING_TRAFFIC);
        return Map.of("readiness", "REFUSING_TRAFFIC", "status", "unready");
    }

    // Operational resilience recovery: Restore readiness accepting traffic
    @PostMapping("/actuator/readiness/accept")
    public Map<String, String> acceptTraffic() {
        AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
        return Map.of("readiness", "ACCEPTING_TRAFFIC", "status", "ready");
    }
}
