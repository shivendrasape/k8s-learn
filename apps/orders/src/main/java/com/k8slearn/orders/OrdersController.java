package com.k8slearn.orders;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OrdersController {

    // Test locally: curl http://localhost:8083/
    @GetMapping("/")
    public Map<String, String> getStatus() {
        return Map.of(
            "service", "orders",
            "status", "ok"
        );
    }
}
