# Catalog Service

Minimal Spring Boot 4 / Java 25 microservice for Kubernetes learning.

## Configuration
- **Port:** `8080`
- **Application Name:** `catalog`
- **Database:** PostgreSQL via Spring Data JPA (`products` table)
- **Environment Variables:**
  - `SPRING_DATASOURCE_URL`: JDBC URL (e.g. `jdbc:postgresql://postgres:5432/catalog`)
  - `SPRING_DATASOURCE_USERNAME`: Database username (injected via Secret)
  - `SPRING_DATASOURCE_PASSWORD`: Database password (injected via Secret)

## Endpoints
- `GET /` - Service status probe (`{"service":"catalog","status":"ok"}`)
- `GET /products` - List all products from the PostgreSQL database
- `POST /products` - Create a new product record

## Build & Run

### 1. Build JAR
```bash
./mvnw clean package -DskipTests
```

### 2. Run locally (requires PostgreSQL running locally or in Docker)
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/catalog \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
./mvnw spring-boot:run
```

### 3. Verify with `curl`
```bash
# Health status
curl http://localhost:8080/

# Query products
curl http://localhost:8080/products
```
