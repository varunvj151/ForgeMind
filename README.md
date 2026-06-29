# ForgeMind

ForgeMind is a modern, real-time Kanban Task Management SaaS platform. It offers a premium user interface with robust backend operations.

## Architecture

- **Frontend:** React, TypeScript, Vite, Tailwind CSS, TanStack Query, Zustand
- **Backend:** Java 21, Spring Boot 3, Spring Security (JWT), Spring Data JPA
- **Database:** PostgreSQL
- **Real-time / Caching:** Redis (STOMP WebSockets)
- **Monitoring:** Prometheus, Grafana, Micrometer Actuator

See [Architecture Documentation](docs/architecture.md) for more details.

## Local Development

### Prerequisites
- Java 21
- Node.js 20
- Docker Desktop (for Postgres and Redis)

### Running Backend
```bash
cd backend
docker-compose up -d  # starts local Postgres & Redis
mvn spring-boot:run
```

### Running Frontend
```bash
cd frontend
npm install
npm run dev
```

## Testing

ForgeMind uses comprehensive testing:
- **Backend:** JUnit 5, Mockito, Testcontainers (Integration)
  - `mvn test` (Unit tests)
  - `mvn test -Dgroups=integration` (Integration tests)
- **Frontend:** Vitest, React Testing Library, MSW
  - `npm run test`
- **E2E:** Playwright
  - `npx playwright test`

---

## Production Deployment Guide

ForgeMind is containerized and ready for deployment using Docker Compose on any VPS (e.g., DigitalOcean, AWS EC2, Linode).

### 1. Prerequisites on the Server
- Install Docker and Docker Compose plugin.
- Clone the repository on the server.

### 2. Configure Environment Variables
Create a `.env` file in the root of the repository:

```env
POSTGRES_USER=forgemind
POSTGRES_PASSWORD=your_secure_db_password
POSTGRES_DB=forgemind_prod

JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### 3. Build and Start Services
Run the following command in the root directory where `docker-compose.prod.yml` is located:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

This command will:
- Build the optimized, multi-stage backend image.
- Build the optimized, Nginx-served frontend SPA image.
- Start PostgreSQL and Redis containers.
- Mount necessary volumes for persistent data.

### 4. Reverse Proxy Setup (Optional but Recommended)
It is highly recommended to set up an NGINX or Caddy reverse proxy in front of the application to handle SSL termination (HTTPS). 
- Route `yourdomain.com/api` and `yourdomain.com/ws` to port `8080` (Backend).
- Route `yourdomain.com` to port `80` (Frontend).

### 5. Monitoring (Optional)
To deploy the monitoring stack:

```bash
docker compose -f docker-compose.monitoring.yml up -d
```
- Access Prometheus on port `9090`.
- Access Grafana on port `3000` (Default credentials: `admin` / `admin`).

## Code Quality & CI/CD
- GitHub Actions automatically runs backend Maven tests, frontend Vitest tests, Playwright E2E tests, type checking, and linting on every PR.
- **Spotless** enforces Google Java Style formatting on the backend.
- **Husky & lint-staged** enforce Prettier and ESLint formatting on the frontend before commits.
