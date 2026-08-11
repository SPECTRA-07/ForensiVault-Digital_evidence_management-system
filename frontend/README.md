# DEMS Frontend - Digital Evidence Management System

Enterprise React + Vite application for the Digital Evidence Management System (DEMS) backend.

## Prerequisites

- **Node.js**: v18.0.0 or higher
- **npm**: v9.0.0 or higher
- **DEMS Spring Boot Backend**: Running at `http://localhost:8080` (or configured via environment)

## Quick Start

1. Install dependencies:
   ```bash
   cd frontend
   npm install
   ```

2. Configure environment:
   Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
   Ensure `VITE_API_BASE_URL` points to your active backend (default: `http://localhost:8080`).

3. Start development server:
   ```bash
   npm run dev
   ```
   Access the application in your browser at: `http://localhost:5173`.

4. Build production bundle:
   ```bash
   npm run build
   ```

## Directory Structure

```
frontend/
├── public/              # Static assets (favicon, logos)
├── src/
│   ├── assets/          # Shared media assets
│   ├── components/      # Foundational UI components (Button, Table, Modal, etc.)
│   ├── constants/       # API endpoints registry (synced with Spring Boot controllers)
│   ├── context/         # AuthContext provider (JWT & role state management)
│   ├── hooks/           # Custom React hooks (useAuth)
│   ├── layouts/         # AppLayout shell (Sidebar, Header with Officer Profile, Main view)
│   ├── pages/           # Module pages (LoginPage, Dashboard, Cases, Evidence, Custody, Audit, QR)
│   ├── routes/          # React Router & ProtectedRoute role authorization
│   ├── services/        # Centralized Axios apiClient & domain API services
│   ├── styles/          # Enterprise CSS design system & tokens
│   ├── utils/           # Error handler & helper utilities
│   ├── App.jsx          # Root application wrapper
│   └── main.jsx         # React DOM rendering entrypoint
├── .env.example         # Environment template
├── .gitignore           # Git exclusion rules
├── index.html           # HTML5 entry document
├── package.json         # NPM package dependencies
└── vite.config.js       # Vite bundler configuration
```

## Authentication & Security Architecture

### Token Persistence Strategy
- Upon successful authentication (`POST /auth/login`), the JWT token is stored in `localStorage` under `dems_auth_token` and user claims (`role`, `employeeId`, `fullName`, `email`, `expiresInMs`) under `dems_user_info`.
- Authentication state is automatically restored on page refresh or browser re-entry by `AuthProvider`.
- Sensitive passwords and credentials are never stored or logged anywhere on the client.

### Axios Interceptors (`apiClient.js`)
- **Request Interceptor**: Dynamically attaches `Authorization: Bearer <token>` to all outgoing backend API requests (except public endpoints).
- **401 Unauthorized Response**: Automatically clears stored local tokens and redirects unauthenticated users to `/login` (with loop prevention).
- **403 Forbidden Response**: Does **NOT** log out the user; allows the application to render a clean, inline Access Denied notification.

### Role-Based Access Control (RBAC)
`AuthContext` provides helper methods `hasRole(role)` and `hasAnyRole(roles)`:
- `ADMIN`: System administration, user management, case creation, officer assignment, QR code regeneration.
- `POLICE_OFFICER`: Case view, evidence uploads, custody handshakes.
- `FORENSIC_EXPERT`: Executive analytics, evidence verification, integrity summary reports.
- `COURT_OFFICIAL`: Read-only timeline audits and verified evidence records.
