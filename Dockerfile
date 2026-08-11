# ===================================================================
# DEMS Multi-Stage Production Dockerfile
# ===================================================================

# Stage 1: Build Jar Artifact using Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production package
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Production JRE 21 Execution Container
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create non-root system group and user for execution security
RUN addgroup -S dems && \
    adduser -S dems -G dems && \
    mkdir -p /app/logs && \
    chown -R dems:dems /app

USER dems:dems

# Copy compiled Spring Boot executable JAR from builder stage
COPY --from=builder /app/target/digital-evidence-management-system-*.jar app.jar

# Environment defaults
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE ${PORT}

# Run Spring Boot application respecting dynamic platform PORT variable
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
