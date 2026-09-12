package com.k8slearn.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CatalogController {

    // Test locally: curl http://localhost:8080/
    @GetMapping("/")
    public Map<String, String> getStatus() {
        return Map.of(
            "service", "catalog",
            "status", "ok"
        );
    }
}
