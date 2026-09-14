# Orders Service

Minimal Spring Boot 4 / Java 25 microservice for Kubernetes learning.

## Configuration
- **Port:** `8083`
- **Application Name:** `orders`
- **Catalog Service URL:** Configured via `CATALOG_URL` environment variable (defaults to `http://localhost:8080`).

## Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/` | Health and basic service status |
| `GET` | `/orders` | Queries `CATALOG_URL/products` via Spring `RestClient` and returns combined order payload |

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
# Basic service health
curl http://localhost:8083/

# Inter-service query (requires catalog running on port 8080, or returns degraded status)
curl http://localhost:8083/orders
```
