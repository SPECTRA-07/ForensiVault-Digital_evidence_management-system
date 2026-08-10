#ForensiVault -  Digital Evidence Management System (DEMS)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## 📌 Project Overview

The **ForensiVault -Digital Evidence Management System (DEMS)** is an enterprise-grade backend application engineered for securely storing, managing, verifying, and tracking digital evidence throughout its legal lifecycle. Designed with clean architecture, high security standards, and scalable modular monolithic principles, DEMS ensures end-to-end evidence integrity, tamper-proof chain of custody, and strict audit compliance.

---

## ⚖️ Business Problem

In law enforcement, legal proceedings, and corporate investigations, digital evidence (CCTV footage, audio recordings, forensic disk images, photos, and scanned documents) faces significant risks:
1. **Chain of Custody Breaches**: Lack of immutable, timestamped tracking of who accessed or transferred evidence.
2. **Data Tampering & Corruption**: Unauthorized file modifications without automatic cryptographic integrity verification.
3. **Unstructured File Storage**: Disorganized local or cloud file storage leading to lost evidence or invalid court admissibility.
4. **Compliance & Audit Failures**: Inability to generate comprehensive, tamper-evident audit logs for legal discovery.

DEMS resolves these challenges by enforcing cryptographic hash verification (SHA-256), automated audit logging, structured role-based access control, and digital chain of custody tracking.

---

## 🏗️ Architecture

Version 1 follows a **Modular Monolith Architecture** adhering to **SOLID principles**, **Clean Architecture**, and strict **Separation of Concerns**.

```
[ HTTP Requests / API Clients ]
              │
              ▼
    ┌──────────────────┐
    │    Controller    │  (REST endpoints & Request Validation)
    └────────┬─────────┘
             │
             ▼
    ┌──────────────────┐
    │     Service      │  (Business Logic & Transaction Boundaries)
    └────────┬─────────┘
             │
             ▼
    ┌──────────────────┐
    │    Repository    │  (Data Access Layer - Spring Data JPA)
    └────────┬─────────┘
             │
             ▼
    ┌──────────────────┐
    │     Database     │  (PostgreSQL Persistence)
    └──────────────────┘
```

- **Constructor Injection Only**: No `@Autowired` field injection.
- **Auditing**: Automated JPA timestamp auditing (`createdAt`, `updatedAt`) via `BaseEntity`.
- **Global Error Handling**: Standardized error responses using `@RestControllerAdvice`.
- **Response Wrapper**: Uniform REST API payloads with `ApiResponse<T>`.

---

## 🛠️ Technology Stack

| Layer / Concern | Technology |
| :--- | :--- |
| **Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.3.x |
| **Security** | Spring Security 6 |
| **Build Tool** | Apache Maven |
| **Database** | PostgreSQL |
| **ORM / Data Access** | Spring Data JPA (Hibernate) |
| **Validation** | Spring Boot Starter Validation (Jakarta Validation) |
| **API Documentation** | Springdoc OpenAPI v2.6.0 (Swagger UI) |
| **Boilerplate Reduction** | Lombok |
| **Monitoring** | Spring Boot Actuator |
| **Logging** | SLF4J + Logback |
| **Testing** | JUnit 5, Mockito, Spring Boot Test, H2 (Test scope) |

---

## 📁 Folder Structure

```
com.dems
│
├── config                 # Security, Swagger, JPA Auditing & Storage Configurations
├── common                 # Standardized ApiResponse wrapper
├── constants              # System-wide static application constants
├── controller             # REST Controllers (Phase 1+)
├── dto                    # Data Transfer Objects
├── entity                 # JPA Entities & MappedSuperclass BaseEntity
├── enums                  # Business Domain Enums
├── exception              # Global Exception Handler & Custom Runtime Exceptions
├── mapper                 # Object Mappers (DTO <-> Entity)
├── repository             # Spring Data JPA Repositories
├── security               # Security Filter Chains & Security Beans
├── service                # Service Interfaces
│     └── impl             # Service Implementations
├── storage                # Local File Storage Abstractions & Properties
├── audit                  # Evidence Audit Trail Engine
├── custody                # Chain of Custody Management
├── util                   # Utility Classes & Helper Functions
├── validation             # Custom Validators & Constraints
└── DigitalEvidenceManagementApplication.java
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK 21+)**
- **Apache Maven 3.8+**
- **PostgreSQL 14+**

### Environment Variables

Configure the following environment variables (or rely on defaults provided in `application-dev.yml`):

| Variable | Description | Default (Dev) |
| :--- | :--- | :--- |
| `PORT` | Server HTTP Port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring Profile | `dev` |
| `DB_HOST` | PostgreSQL Host | `localhost` |
| `DB_PORT` | PostgreSQL Port | `5432` |
| `DB_NAME` | Database Name | `dems_db` |
| `DB_USERNAME` | Database User | `postgres` |
| `DB_PASSWORD` | Database Password | `postgres` |
| `STORAGE_LOCATION` | Local File Upload Root Path | `uploads` |

---

## 🏃 Running the Project

### 1. Build and Test

Compile the project and run unit tests:
```bash
mvn clean compile
mvn test
```

### 2. Run Locally (Dev Profile)

Make sure your local PostgreSQL database is running, then execute:
```bash
mvn spring-boot:run
```
Alternatively, run with explicit profile setting:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 📖 Swagger & Health Documentation

Once the application is running, access the OpenAPI documentation and health endpoints:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Specs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Actuator Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 🎯 Version 1 Scope

- Enterprise production-ready modular monolith foundation.
- PostgreSQL database integration & JPA auditing.
- Uniform API response wrapper (`ApiResponse<T>`).
- Centralized exception framework (`GlobalExceptionHandler`, `ErrorResponse`).
- Interactive Swagger documentation.
- Local storage directory structure setup (`uploads/cases/`, `uploads/temp/`, `uploads/qr/`).
- Actuator health check endpoint.

---

## 🔮 Future Roadmap

- **Phase 1**: Authentication & Authorization (JWT, RBAC, User & Role Management).
- **Phase 2**: Case & Evidence Management (Metadata, Storage, SHA-256 Hashing).
- **Phase 3**: Chain of Custody, Audit Log Verification, and QR Code generation.
- **Future Enhancements**: AWS S3 Storage Provider, Redis Caching, Kafka Event Streaming, Dockerization, OCR & AI Evidence Analysis.

---

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).
