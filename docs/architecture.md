# Architecture

- Frontend
  - Web application: React, TypeScript, Vite
  - Client state management: Zustand
  - API client: Native Fetch API
  - Server state management: TanStack Query
  - Client routing: React Router
  - UI component library: Mantine
  - Styling and theming: Mantine styles and theme
  - Icons: Tabler Icons

- Backend
  - Framework: Java, Spring Boot
  - Background jobs: Spring TaskExecutor
  - ORM: JPA, Hibernate
  - Schema migrations: Flyway

- Database
  - Relational database: PostgreSQL
  - Hosting: Aiven

- Object Storage
  - API: S3
  - Provider: IDrive e2

- Authentication and Authorization
  - Identity management: Keycloak
  - Backend authorization: Spring Security

- Infrastructure and Deployment
  - Backend hosting: Render
  - Identity hosting: Render
  - Reverse proxy and TLS: Render-managed HTTPS
  - Configuration and secrets: Render environment variables
  - CI/CD: GitHub Actions

- Deferred
  - Image generation provider: None for now
  - Logging and monitoring: None for now
  - Search engine: Deferred