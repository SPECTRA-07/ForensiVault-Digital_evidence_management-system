# Implementation Plan - Digital Evidence Management System (DEMS) Foundation

Establish an enterprise-grade, production-ready Spring Boot 3.3.x foundation for the Digital Evidence Management System (DEMS) using Java 21, Maven, PostgreSQL, Spring Security, Validation, Lombok, Springdoc OpenAPI, and Spring Boot Actuator.

## User Review Required

> [!NOTE]
> This phase focuses **strictly on the production-ready technical foundation**. No business entities, JWT authentication logic, controllers, or service implementations will be created in this phase.
>
> An H2 in-memory database dependency is included in `<scope>test</scope>` to ensure `mvn clean compile` and `mvn test` run successfully without requiring a live PostgreSQL instance running during build.

## Proposed Changes

### Build & Root Configuration

#### [NEW] [pom.xml](file:///c:/Users/ritik/Desktop/evidence_tracker/pom.xml)
- Configure Java 21, Spring Boot 3.3.5 starter parent.
- Add dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-actuator`, `postgresql`, `lombok`, `spring-boot-devtools`, `springdoc-openapi-starter-webmvc-ui` (v2.6.0), `spring-boot-starter-test`, and `h2` (test scope).
- Configure `spring-boot-maven-plugin` and `maven-compiler-plugin` with Lombok annotation processor.

#### [NEW] [.gitignore](file:///c:/Users/ritik/Desktop/evidence_tracker/.gitignore)
- Ignore `target/`, IDE files (`.idea/`, `.vscode/`, `*.iml`), logs (`logs/`), environment files (`.env`), and dynamic upload files while keeping `.gitkeep` markers for non-code folders (`uploads/` and `docs/`).

#### [NEW] [README.md](file:///c:/Users/ritik/Desktop/evidence_tracker/README.md)
- Complete enterprise documentation detailing Project Overview, Business Problem, Layered Architecture, Technology Stack, Folder Structure, Getting Started, Environment Variables, Running the Project, Swagger Documentation, Actuator Health Checks, Version 1 Scope, Future Roadmap, and License.

---

### Storage & Documentation Directories (Non-Code Directories with `.gitkeep`)

#### [NEW] Storage Directory Structure
- `uploads/.gitkeep`
- `uploads/cases/.gitkeep`
- `uploads/temp/.gitkeep`
- `uploads/qr/.gitkeep`

#### [NEW] Documentation Directory Structure
- `docs/README.md` (placeholders for Architecture Diagram, ER Diagram, API Flow Diagram, Sequence Diagrams)

---

### Application Configuration & Logging

#### [NEW] [application.yml](file:///c:/Users/ritik/Desktop/evidence_tracker/src/main/resources/application.yml)
- Root configuration setting active profile `dev`, server port `${PORT:8080}`, application name `digital-evidence-management-system`.
- Multipart file limits increased to **100MB** (`max-file-size: 100MB`, `max-request-size: 100MB`) to accommodate videos & ZIP evidence files.
- Expose **only health endpoint** for Spring Boot Actuator (`management.endpoints.web.exposure.include: health`).
- Custom storage properties (`app.storage.*`) and Springdoc OpenAPI configuration.

#### [NEW] [application-dev.yml](file:///c:/Users/ritik/Desktop/evidence_tracker/src/main/resources/application-dev.yml)
- Environment-driven PostgreSQL datasource configuration (`${DB_HOST}`, `${DB_PORT}`, `${DB_NAME}`, `${DB_USERNAME}`, `${DB_PASSWORD}`).
- Hibernate settings (`ddl-auto: update`, `show-sql: true`, `format_sql: true`, `use_sql_comments: true`).

#### [NEW] [application-prod.yml](file:///c:/Users/ritik/Desktop/evidence_tracker/src/main/resources/application-prod.yml)
- Enterprise production configuration template with optimized connection pooling (HikariCP) and secure logging levels.

#### [NEW] [application-test.yml](file:///c:/Users/ritik/Desktop/evidence_tracker/src/main/resources/application-test.yml)
- Embedded H2 database configuration for reliable automated testing during build.

#### [NEW] [logback-spring.xml](file:///c:/Users/ritik/Desktop/evidence_tracker/src/main/resources/logback-spring.xml)
- Logback configuration writing structured application logs to `logs/app.log` with daily rolling policy, console appender, and environment-aware log levels (INFO/WARN/ERROR).

---

### Java Package Structure (`com.dems`)

#### [NEW] Main Application Class
- `src/main/java/com/dems/DigitalEvidenceManagementApplication.java`

#### [NEW] Configuration Package (`com.dems.config`)
- `ApplicationConfig.java`: Configures `@EnableJpaAuditing` and general bean definitions.
- `SwaggerConfig.java`: Springdoc `OpenAPI` bean defining DEMS metadata, API title, v1 version, and license.
- `StorageProperties.java`: Encapsulates file storage configuration using `@ConfigurationProperties(prefix = "app.storage")`.

#### [NEW] Entity Package (`com.dems.entity`)
- `BaseEntity.java`: JPA `@MappedSuperclass` with `@EntityListeners(AuditingEntityListener.class)` tracking `createdAt` and `updatedAt` (`OffsetDateTime`).

#### [NEW] Security Package (`com.dems.security`)
- `SecurityConfig.java`: Spring Security 6 filter chain configuring `permitAll()` for all endpoints, Swagger UI, and Actuator health endpoint, disabling CSRF for REST APIs.

#### [NEW] Constants Package (`com.dems.constants`)
- `ApplicationConstants.java`: System-wide constants (`APPLICATION_NAME`, `API_VERSION`, `DEFAULT_UPLOAD_DIRECTORY`, etc.).

#### [NEW] Common Models Package (`com.dems.common`)
- `ApiResponse.java`: Generic REST response wrapper with fields `success`, `message`, `data`, `timestamp` and static builder factory methods.

#### [NEW] Exception Handling Package (`com.dems.exception`)
- `ErrorResponse.java`: Unified error payload model containing `timestamp`, `status`, `error`, `message`, `path`, and `validationErrors`.
- `GlobalExceptionHandler.java`: `@RestControllerAdvice` handling custom exceptions, validation errors (`MethodArgumentNotValidException`), and unexpected server errors.
- Exception Classes:
  - `ResourceNotFoundException.java`
  - `BadRequestException.java`
  - `ConflictException.java`
  - `UnauthorizedException.java`
  - `InternalServerException.java`

---

### Automated Verification & Testing

#### [NEW] [ContextLoadsTest.java](file:///c:/Users/ritik/Desktop/evidence_tracker/src/test/java/com/dems/ContextLoadsTest.java)
- `@SpringBootTest` verifying Spring ApplicationContext initializes cleanly.

## Verification Plan

### Automated Tests
- Run `mvn clean compile` to ensure zero compilation errors.
- Run `mvn test` to verify the Spring ApplicationContext loads cleanly.

### Manual Verification
- Verify directory structure matches exact specifications (no `.gitkeep` inside Java packages).
- Check `.gitignore`, `pom.xml`, Logback, OpenAPI, Actuator, and YAML configuration files.
