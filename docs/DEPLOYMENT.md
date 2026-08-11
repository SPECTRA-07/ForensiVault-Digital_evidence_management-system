# DEMS Production Deployment Guide & Safety Checklist

This guide documents the production deployment architecture, environment setup, database migrations, persistent object storage configuration, CORS setup, Vercel SPA setup, and pre-deployment validation checklist for the Digital Evidence Management System (DEMS).

---

## 1. Deployment Architecture Overview

```
React Frontend (Vercel SPA)
        │
        ▼  HTTPS / REST / CORS (APP_CORS_ALLOWED_ORIGINS)
Spring Boot Monolith API (Docker Container / Cloud Host)
        │
        ├──► PostgreSQL Production Database (Metadata & Audit Logs)
        │
        └──► Persistent Object Storage (AWS S3 / Cloudflare R2 / MinIO)
                ├── cases/{caseNumber}/{storedFileName}  (Evidence Payload Files)
                └── qr/QR-{evidenceNumber}.png           (Physical Barcode Images)
```

---

## 2. Environment Variables Directory

Configure the following environment variables in your cloud container host and Vercel project settings:

| Variable Name | Required | Default / Example | Purpose |
| :--- | :---: | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Yes | `prod` | Activates production Spring profile (`application-prod.yml`) |
| `PORT` | Yes | `8080` | Dynamic HTTP server port binding |
| `DB_HOST` | Yes | `postgres.your-provider.com` | PostgreSQL database hostname |
| `DB_PORT` | Yes | `5432` | PostgreSQL database port |
| `DB_NAME` | Yes | `dems_db` | PostgreSQL database name |
| `DB_USERNAME` | Yes | `dems_prod_user` | PostgreSQL database user |
| `DB_PASSWORD` | Yes | `[SECURE_DB_PASSWORD]` | PostgreSQL database password |
| `DB_SSL_MODE` | No | `require` | PostgreSQL SSL connection mode |
| `JWT_SECRET` | Yes | `[64_HEX_CHAR_SECRET]` | HMAC-SHA256 secret key for signing JWT tokens |
| `JWT_EXPIRATION_MS` | No | `86400000` (24h) | JWT token expiration in milliseconds |
| `STORAGE_PROVIDER` | Yes | `s3` | Storage engine selection (`local` or `s3`) |
| `STORAGE_BUCKET` | Yes | `dems-evidence-prod` | S3-compatible object storage bucket name |
| `STORAGE_REGION` | Yes | `us-east-1` | AWS / S3 region identifier |
| `STORAGE_ENDPOINT` | Optional | `https://<account_id>.r2.cloudflarestorage.com` | Custom endpoint for Cloudflare R2 / MinIO / DigitalOcean |
| `AWS_ACCESS_KEY_ID` | Yes | `[S3_ACCESS_KEY_ID]` | Resolved by AWS SDK DefaultCredentialsProvider |
| `AWS_SECRET_ACCESS_KEY` | Yes | `[S3_SECRET_ACCESS_KEY]` | Resolved by AWS SDK DefaultCredentialsProvider |
| `APP_CORS_ALLOWED_ORIGINS` | Yes | `https://dems-frontend.vercel.app` | Production CORS allowed origin domain |

---

## 3. Step-by-Step Production Deployment Sequence

Follow this exact sequence when deploying DEMS to production:

1. **Provision PostgreSQL Database**: Create database `CREATE DATABASE dems_db;` on your cloud PostgreSQL provider.
2. **Provision Object Storage Bucket**: Create a private S3/R2 bucket (e.g. `dems-evidence-prod`).
3. **Configure AWS/R2 Credentials**: Provision an IAM / API key with `s3:GetObject`, `s3:PutObject`, `s3:DeleteObject` permissions.
4. **Configure Container Environment**: Populate container environment variables (`DB_HOST`, `DB_PASSWORD`, `JWT_SECRET`, `STORAGE_PROVIDER=s3`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`).
5. **Deploy Backend Container**: Build and deploy `Dockerfile` or `Dockerfile.vercel` to container hosting platform.
6. **Obtain Backend Production Domain**: Copy backend production URL (e.g. `https://dems-api.yourdomain.com`).
7. **Configure Production CORS**: Set `APP_CORS_ALLOWED_ORIGINS=https://dems-frontend.vercel.app` on backend container.
8. **Configure Vercel Environment**: In Vercel Project Settings, set `VITE_API_BASE_URL=https://dems-api.yourdomain.com`.
9. **Deploy Frontend to Vercel**: Connect Git repository to Vercel using `frontend/` as root directory.
10. **Verify Health Endpoint**: Query `GET /actuator/health` to confirm HTTP 200 OK health status.
11. **Verify Authentication & RBAC**: Test login as `ADMIN` and `POLICE_OFFICER`. Confirm role landing redirects.
12. **Verify Evidence Upload**: Register an evidence file payload and confirm upload to S3/R2 object storage.
13. **Verify Payload Download**: Download evidence file payload stream and confirm original file name preservation.
14. **Verify SHA-256 Integrity**: Execute **Verify Integrity** and confirm hash matching against stored object.
15. **Verify Custody Handshake**: Initiate and accept custody transfer between officers.
16. **Verify QR Code Tracking & Audit Trail**: Stream 250x250 PNG QR tag and verify read-only audit log events.

---

## 4. Deployment Pre-Flight Safety Checklist

- [ ] All production secrets (`DB_PASSWORD`, `JWT_SECRET`, `AWS_SECRET_ACCESS_KEY`) configured in environment settings.
- [ ] No `.env` or credentials files checked into Git source control.
- [ ] Vercel SPA rewrites configured (`frontend/vercel.json`).
- [ ] Port binding supports dynamic platform `$PORT` (`server.port=${PORT:8080}`).
- [ ] Actuator exposes `/actuator/health` only (`management.endpoints.web.exposure.include=health`).
- [ ] CORS restricted to production Vercel domain (`APP_CORS_ALLOWED_ORIGINS`).
- [ ] `STORAGE_PROVIDER=s3` selected for cloud deployment.
- [ ] SHA-256 integrity verification passes cleanly on stored evidence objects.
- [ ] QR code barcode scanning & streaming (`/qr/evidence/{id}/image`) works properly through storage service.
