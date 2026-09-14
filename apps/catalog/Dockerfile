# Stage 1: Build the application JAR using native host platform (avoids QEMU untar bug)
FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

# Copy wrapper and configuration first for dependency layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy source code and package application
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal JRE runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app

# Install curl for in-cluster network debugging and verification
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Copy the built fat JAR from the builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Expose catalog port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
