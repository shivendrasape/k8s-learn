# Orders Service

Minimal Spring Boot 4 / Java 25 microservice for Kubernetes learning.

## Configuration
- **Port:** `8083`
- **Application Name:** `orders`

## Build & Run

### 1. Build JAR
```bash
./mvnw clean package -DskipTests
```

### 2. Run locally
```bash
./mvnw spring-boot:run
```

### 3. Verify with `curl`
```bash
curl http://localhost:8083/
```

**Expected Response:**
```json
{"service":"orders","status":"ok"}
```
