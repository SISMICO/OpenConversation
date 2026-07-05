# AGENT.md

## Project Overview

This is a **language learning feedback platform**. A user has a spoken conversation (in a language they choose, e.g. English or Portuguese) about a topic they define (e.g. "simulate a job interview about my career"). The system records the audio, transcribes it, analyzes it with a local LLM, and returns feedback on vocabulary, grammar, verb tense, and overall fluency. All feedback and transcripts are persisted so the user can track improvement over time.

- **Frontend:** Vite + React (`webapp/`)
- **Backend:** Kotlin + Spring Boot (`backend/`)
- **Speech-to-text:** Whisper (containerized, exposed via HTTP API)
- **LLM analysis:** Ollama / llama.cpp (containerized, exposed via HTTP API), running local models initially
- **Database:** PostgreSQL, migrated with Flyway
- **Infra:** Docker Compose for local dev; designed so Postgres and app containers could later move to dedicated servers, with a future path to a cloud-hosted SaaS

## Project Structure

Two independent applications, no monorepo:

- `backend/` - Kotlin + Spring Boot application (hexagonal architecture)
- `webapp/` - Vite + React frontend
- `infra/` - Docker Compose files, Dockerfiles, and environment configuration for Postgres, Whisper, and Ollama/llama.cpp services

### Backend package layout (ports and adapters / hexagonal)

Proposed structure inside `backend/src/main/kotlin/...`:

- `domain/` - Core business logic, entities, and value objects (e.g. `Conversation`, `Feedback`, `Topic`), framework-agnostic
- `application/` - Use cases / services orchestrating domain logic (e.g. `AnalyzeConversationUseCase`), defines **port interfaces** here (`in` for use cases, `out` for dependencies like `TranscriptionPort`, `LlmAnalysisPort`, `ConversationRepositoryPort`)
- `adapters/in/web/` - REST controllers, request/response DTOs (inbound adapters)
- `adapters/out/persistence/` - Postgres/JPA repository implementations, Flyway migrations reference (outbound adapters)
- `adapters/out/transcription/` - Whisper HTTP client implementing `TranscriptionPort`
- `adapters/out/storage/` - Audio storage adapter implementing `AudioStoragePort` (local filesystem for now; designed so S3/object storage can be swapped in later)
- `adapters/out/llm/` - Ollama/llama.cpp HTTP client implementing `LlmAnalysisPort`
- `config/` - Spring configuration, bean wiring

This keeps `domain/` and `application/` free of Spring/HTTP/DB dependencies, with all I/O isolated in `adapters/`.

## Architecture Decisions

- **Ports and adapters (hexagonal architecture)** for the backend, keeping domain logic independent of frameworks and external services.
- **REST APIs** for all communication (frontend-backend, backend-Whisper, backend-Ollama/llama.cpp) — kept simple and direct, no gRPC/GraphQL for now.
- **Whisper and Ollama/llama.cpp run as separate Docker containers**, each exposing a web API; the backend calls them as external HTTP services rather than embedding them.
- **Local-first models**: the system runs entirely with local models (Whisper + Ollama/llama.cpp) at this stage. A cloud-hosted SaaS version is a future possibility, not a current requirement.
- **Language and topic are user-selected inputs** from the webapp (language via dropdown, topic via free-text phrase), passed to the backend to drive both the LLM analysis prompt and the persisted history record.
- **Flow:** webapp records audio -> sends audio + language + topic to backend REST API -> backend calls Whisper for transcription -> backend calls Ollama/llama.cpp for analysis -> backend persists transcript, feedback, and topic in Postgres -> backend returns feedback to webapp.

## Code Standards

### JavaScript/TypeScript (webapp)

- Use TypeScript with strict mode where possible
- Prefer ESM imports over CommonJS require
- Use async/await over raw Promises
- Use Vitest + React Testing Library for tests

### Kotlin (backend)

- Use data classes for DTOs and domain value objects
- Prefer val over var
- Use coroutines for async operations (e.g. calling Whisper/Ollama APIs)
- Keep domain and application layers free of Spring annotations
- Use JUnit + MockK for tests

### Documentation / Markdown

- Use consistent heading levels
- Keep line length under 120 characters where practical
- Include code examples for technical documentation

### Docker / Containers

- Use multi-stage builds for smaller images
- Pin base image versions
- Do not run as root in containers
- Each service (backend, Whisper, Ollama/llama.cpp, Postgres) runs as its own container in `infra/docker-compose.yml`

### SQL / Database

- Use Flyway migrations for all schema changes
- Add indexes for frequently queried columns (e.g. user id, conversation date)
- Use parameterized queries to prevent SQL injection
- Core tables anticipated: users/sessions, conversations (audio ref, transcript, topic, language), feedback (per conversation), topics/languages as reference data

## Commands

### Frontend (webapp)

- **Build:** `npm run build`
- **Test:** `npm test`
- **Lint:** `npm run lint`

### Backend (backend)

- **Build:** `gradle build`
- **Test:** `gradle test`
- **Lint:** `gradle ktlintCheck`

### Documentation / Markdown

- **Lint:** `markdownlint "**/*.md"`

### Docker / Containers

- **Build:** `docker compose -f infra/docker-compose.yml build`
- **Up:** `docker compose -f infra/docker-compose.yml up`
- **Lint:** `hadolint Dockerfile`

### SQL / Database

- **Lint:** `sqlfluff lint`
- **Migrate:** `gradle flywayMigrate`

## Custom Agents

Invoke with `@agent-name`:

- **@backend-developer**: Server-side logic, APIs, and data processing
- **@frontend-developer**: UI implementation, components, and browser APIs
- **@fullstack-developer**: End-to-end feature development across the stack
- **@typescript-pro**: TypeScript type system, generics, and advanced patterns
- **@kotlin-specialist**: Kotlin idioms, coroutines, hexagonal architecture patterns
- **@react-specialist**: React hooks, state management, and component design
- **@spring-boot-engineer**: Spring Boot auto-config, DI, and reactive stack
- **@database-administrator**: Database provisioning, replication, and backup strategy
- **@docker-expert**: Docker optimization, multi-stage builds, and security
- **@code-reviewer**: Code review with security and performance focus
- **@database-optimizer**: Query optimization, indexing, and schema design
- **@postgres-pro**: PostgreSQL tuning, extensions, and advanced SQL
- **@sql-pro**: Advanced SQL queries, window functions, and optimization
- **@build-engineer**: Build system configuration, caching, and optimization
- **@dependency-manager**: Dependency updates, audit, and compatibility checks
- **@docs-writer**: Technical documentation and API reference writing
- **@git-workflow-manager**: Git workflow, branching strategy, and commit hygiene
- **@test-writer**: Test generation following project patterns
- **@api-designer**: REST API design and contract definition

## Available Skills

- **git-release**: Release notes and version bumps
- **test-patterns**: Test generation following project conventions (JUnit/MockK, Vitest)
- **dependency-audit**: Audit dependencies for vulnerabilities and license issues
- **changelog-generate**: Changelog generation from commit history
- **ci-pipeline**: CI pipeline configuration and optimization

## Conventions

- Use conventional commits: `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`
- Write meaningful commit messages that explain the "why"
- Keep PRs focused on a single concern
