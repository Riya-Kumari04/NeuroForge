# NeuroForge — Final Complete

An enterprise SDLC platform with multi-role RBAC, organization management, project & sprint tracking.

## Structure

```
NeuroForge-Final-Complete/
├── NeuroForge-Backend/    ← Spring Boot 3 + Java 17 + PostgreSQL
└── NeuroForge-Frontend/   ← React + TypeScript + Vite + Tailwind CSS
```

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- PostgreSQL 14+

### Backend

```bash
cd NeuroForge-Backend

# Create the database
createdb neuroforge   # or use pgAdmin / DBeaver

# Configure connection (optional — defaults shown):
# DB_URL=jdbc:postgresql://localhost:5432/neuroforge
# DB_USERNAME=postgres
# DB_PASSWORD=postgres
# MAIL_USERNAME=your@gmail.com
# MAIL_PASSWORD=your-app-password

# Run
./mvnw spring-boot:run
# Server starts on http://localhost:8081
# Swagger UI: http://localhost:8081/swagger-ui.html
```

### Frontend

```bash
cd NeuroForge-Frontend
npm install
npm run dev
# App starts on http://localhost:5173
```

## Roles

| Role             | Access level |
|------------------|-------------|
| Super Admin      | Full platform control |
| Organization Admin | Manage one org |
| Project Manager  | Create/manage projects & sprints |
| Developer        | View assigned projects & tasks |
| QA / Tester      | View testing projects |
| Client           | Read-only project view |

## Modules Implemented

- **Module 1** — Auth, JWT, RBAC, OTP verification, password reset
- **Module 2** — Organizations, Teams, Members, Invitations
- **Module 3** — Projects, Sprints, Tasks, Portfolio

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/neuroforge` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | DB username |
| `DB_PASSWORD` | `postgres` | DB password |
| `JWT_SECRET` | (built-in) | HS256 secret key |
| `MAIL_USERNAME` | — | Gmail address for sending OTP/invites |
| `MAIL_PASSWORD` | — | Gmail app password |
| `PORT` | `8081` | Backend server port |
