# SpeisList — Agent Instructions

Collaborative grocery-list and pantry-inventory web app.  
Monorepo with three top-level directories: `backend-service/`, `frontend-service/`, `keycloak/`.

---

## Project Overview

| Layer | Tech |
|---|---|
| Backend | Java 25, Spring Boot 3.5.6, Spring Data JPA, Spring Security (OAuth2 JWT) |
| Backend AI | Spring AI 1.1.0-M4 MCP server (Streamable HTTP on `/mcp`) |
| Database | H2 (dev/test), PostgreSQL (prod) |
| Frontend | TypeScript 5.7, React 19, TanStack Start/Router/Query, Tailwind CSS v4 |
| Auth | Keycloak (Docker Compose in `keycloak/`) |
| Build (BE) | Gradle (wrapper at `backend-service/gradlew`) |
| Build (FE) | Vite 7, pnpm |
| Testing (BE) | JUnit 5 + Mockito + AssertJ |
| Testing (FE) | Vitest + Testing Library |
| Lint/Format (BE) | Spotless + Palantir Java Format |
| Lint/Format (FE) | Biome 2.2.4 |

---

## Subdirectory Instructions

Each service has its own `AGENTS.md` with commands, code style, and testing guidelines:

- **[backend-service/AGENTS.md](backend-service/AGENTS.md)** — Java/Spring Boot service
- **[frontend-service/AGENTS.md](frontend-service/AGENTS.md)** — React/TypeScript frontend
