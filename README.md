# Digital Evidence Management System (DEMS)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-6.4.3-purple.svg)](https://vitejs.dev/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## 📌 Project Overview

The **Digital Evidence Management System (DEMS)** is an enterprise-grade, high-security application engineered for securely storing, managing, verifying, and tracking digital evidence throughout its legal lifecycle. Designed with clean modular monolithic architecture and strict security standards, DEMS ensures end-to-end evidence integrity, tamper-proof chain of custody, QR barcode tracking, and comprehensive audit compliance.

---

## 🏛️ System Architecture

DEMS is built as a **Modular Monolithic Architecture**:

```
                  React 18 + Vite 6 SPA Frontend (Vercel)
                                    │
                                    ▼  HTTPS / REST / JWT Bearer
                  Spring Boot 3.3.5 Monolith Backend (Docker)
                                    │
    ┌───────────────────────────────┼───────────────────────────────┐
    ▼                               ▼                               ▼
Spring Security 6            PostgreSQL Database             Storage Provider
(JWT Authentication)       (Metadata & Audit Logs)            Abstraction
                                                                    │
                                                     ┌──────────────┴──────────────┐
                                                     ▼                             ▼
                                               LocalStorage                 AWS S3 / R2 Object
                                               (Development)               Storage (Production)
```

- **Monolithic Core**: Single deployable Spring Boot unit (`digital-evidence-management-system-1.0.0-SNAPSHOT.jar`) encapsulating all business domains.
- **Relational Database**: PostgreSQL stores cases, evidence metadata, custody transfers, user accounts, and immutable audit logs.
- **Persistent Object Storage**: Pluggable storage abstraction supporting local disk storage for development and AWS S3 / Cloudflare R2 for production object storage.
- **SHA-256 Integrity Verification**: Real-time streaming hash recalculation verifying physical files against baseline cryptographic hashes.

---

## 🛠️ Technology Stack

| Layer / Concern | Technology |
| :--- | :--- |
| **Frontend Framework** | React 18 SPA + Vite 6.4.3 |
| **Frontend Styling** | Vanilla CSS Design System with Glassmorphic UI |
| **Backend Framework** | Spring Boot 3.3.5 (Java 21 LTS) |
| **Security & Auth** | Spring Security 6 + JJWT 0.12.6 (HMAC-SHA256) |
| **Database & ORM** | PostgreSQL 15+ & Hibernate ORM |
| **Object Storage** | AWS SDK for Java 2.x (`software.amazon.awssdk:s3`) |
| **Barcode Generator** | ZXing 3.5.3 (`core` & `javase`) |
| **API Documentation** | Springdoc OpenAPI v2.6.0 (Swagger UI) |
| **Monitoring** | Spring Boot Actuator (`/actuator/health`) |

---

## 🛡️ Role-Based Access Control (RBAC) Matrix

| System Module | `ADMIN` | `FORENSIC_EXPERT` | `POLICE_OFFICER` | `COURT_OFFICIAL` |
| :--- | :---: | :---: | :---: | :---: |
| **Executive Dashboard** (`/dashboard`) | ✅ Full Access | ✅ Full Access | ❌ Restricted (403) | ❌ Restricted (403) |
| **Case Management** (`/cases`) | ✅ Full + Create | 👁️ Read-Only | 👁️ Read-Only | 👁️ Read-Only |
| **Officer Assignment** (`PATCH /cases/{id}/assign-officer`) | ✅ Admin Only | ❌ Restricted | ❌ Restricted | ❌ Restricted |
| **Evidence Upload & Management** (`/evidence`) | ✅ Full Access | ✅ Full Access | ✅ Assigned Cases | 👁️ Read-Only |
| **SHA-256 Integrity Verification** (`/integrity`) | ✅ Full Access | ✅ Full Access | ✅ Verify Permitted | 👁️ Read-Only |
| **Chain of Custody** (`/custody`) | ✅ Full Access | ✅ Full Access | ✅ Initiate & Accept | 👁️ Read-Only |
| **Physical QR Barcode Tracking** (`/qr`) | ✅ Full + Regenerate | ✅ View Tag | ✅ View Tag | 👁️ View Tag |
| **Forensic Audit Logs** (`/audit`) | 👁️ Read-Only | 👁️ Read-Only | 👁️ Read-Only | 👁️ Read-Only |
| **User Account Management** (`/users`) | ✅ Admin Only | ❌ Restricted (403) | ❌ Restricted (403) | ❌ Restricted (403) |

---

## 🔑 Environment Variables Directory

| Variable | Purpose | Default (Dev) | Production Example |
| :--- | :--- | :--- | :--- |
| `PORT` | HTTP Server Binding Port | `8080` | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring Profile | `dev` | `prod` |
| `DB_HOST` | PostgreSQL Hostname | `localhost` | `postgres.your-cloud-provider.com` |
| `DB_PORT` | PostgreSQL Port | `5432` | `5432` |
| `DB_NAME` | Database Name | `dems_db` | `dems_db` |
| `DB_USERNAME` | Database User | `postgres` | `dems_prod_user` |
| `DB_PASSWORD` | Database Password | `root` | `[SECURE_PROD_PASSWORD]` |
| `JWT_SECRET` | 256-bit Hex Signing Key | Dev Secret | `[64_HEX_CHAR_SECRET]` |
| `STORAGE_PROVIDER` | Storage Engine Selection | `local` | `s3` |
| `STORAGE_BUCKET` | S3 Storage Bucket Name | `dems-evidence` | `dems-evidence-prod` |
| `STORAGE_REGION` | AWS S3 Region | `us-east-1` | `us-east-1` |
| `APP_CORS_ALLOWED_ORIGINS` | CORS Allowed Origins | `http://localhost:5173` | `https://dems-frontend.vercel.app` |

---

## 🏃 Local Development Quickstart

### 1. Start Backend Monolith
```bash
# Compile and run Spring Boot server (port 8080)
mvn clean spring-boot:run
```
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health Endpoint: `http://localhost:8080/actuator/health`

### 2. Start Frontend SPA
```bash
cd frontend
npm install
npm run dev
```
- Frontend Dev Server: `http://localhost:5173`

---

## 🧪 Testing & Build Verification

```bash
# Run 100% backend unit & integration test suite (59 tests)
mvn clean test

# Build production frontend bundle
cd frontend && npm run build

# Package executable production Spring Boot JAR
mvn clean package -DskipTests
```

For complete cloud deployment instructions to Vercel and Docker hosts, see [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md).
