# NeuroForge Backend — Merged Monolith

Spring Boot 3.3.4 / Java 17 monolith combining all three modules.

## What's included

### Module 1 — Auth & Security
- JWT authentication (access + refresh tokens)
- OTP-based email verification and password reset
- Role-based access control (`ROLE_SUPER_ADMIN`, `ROLE_ORG_ADMIN`, `ROLE_PROJECT_MANAGER`, `ROLE_DEVELOPER`, `ROLE_TESTER`)
- In-memory rate limiting via Bucket4j (`RateLimitFilter`)
- Swagger / OpenAPI docs at `/swagger-ui.html`

### Module 2 — Organization & Team Workspace
- Organization CRUD + slug-based lookup
- Team management within organizations
- Team membership with org-scoped roles
- Invite flow (email invite → token → accept/reject)

### Module 3 — Projects, Sprints & Tasks
- Project CRUD with portfolio and dashboard views
- Sprint management (PLANNED → ACTIVE → COMPLETED)
- Task management with priority, status, sprint and assignee
- Project members (backed by team_members) with project-scoped roles
- `GET /api/projects/organization/{id}` — projects by org
- `GET /api/projects/{id}/stats` — task counts by status
- Granular security: tasks visible to DEVELOPER and TESTER roles

## Running locally

```bash
# Prerequisites: Java 17, Maven, PostgreSQL
mvn spring-boot:run
```

### Environment variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/neuroforge` | JDBC URL |
| `DB_USERNAME` | `postgres` | DB user |
| `DB_PASSWORD` | `postgres` | DB password |
| `JWT_SECRET` | (long default) | HS256 signing key |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | — | Gmail address |
| `MAIL_PASSWORD` | — | Gmail App Password |
| `PORT` | `8081` | Server port |

## Database setup

Run scripts in order against your PostgreSQL database:

```bash
# Module 2: organizations, teams, team_members, invites
psql -d neuroforge -f database/module2_schema.sql

# Module 3: projects, sprints, project_members, tasks
psql -d neuroforge -f database/module3_schema.sql
```

The Module 1 schema (users, otps) is created automatically by Hibernate (`ddl-auto=update`).

## API endpoints summary

| Prefix | Controller | Auth required |
|---|---|---|
| `/auth/**` | AuthController | Public |
| `/api/users/**` | UserController | Authenticated |
| `/api/organizations/**` | OrganizationController | ORG_ADMIN+ |
| `/api/invites/**` | InviteController | ORG_ADMIN+ |
| `/api/dashboard/**` | DashboardController | Authenticated |
| `/api/projects/**` | ProjectController | PROJECT_MANAGER+ |
| `/api/sprints/**` | SprintController | PROJECT_MANAGER+ |
| `/api/tasks/**` | TaskController | DEVELOPER+ |
| `/api/project-members/**` | ProjectMemberController | PROJECT_MANAGER+ |

Swagger UI: `http://localhost:8081/swagger-ui.html`
