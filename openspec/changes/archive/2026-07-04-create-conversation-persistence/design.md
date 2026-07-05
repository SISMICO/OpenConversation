## Context

The backend is a greenfield Kotlin/Spring Boot application following hexagonal architecture. The frontend can record audio and send it to a mock `/api/analyse` endpoint, but nothing is persisted yet. This change introduces the first persistence layer so topics, transcripts, and feedback survive beyond a single request.

## Goals / Non-Goals

**Goals:**
- Define and create the PostgreSQL schema for topics, conversations, and feedback items using Flyway.
- Implement domain models, repository ports, JPA entities, adapters, and mappers aligned with hexagonal architecture.
- Implement topic upsert logic in the application layer.
- Wire persistence into the existing analysis endpoint so every analyzed recording is stored.

**Non-Goals:**
- Implementing real audio file storage (only a reference string is persisted).
- Integrating real Whisper or LLM services (the mock analysis flow remains).
- Adding user authentication or multi-tenancy.
- Adding pagination, sorting beyond creation date, or advanced search beyond topic title.

## Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Persistence stack | Spring Data JPA + Flyway | Matches existing Spring Boot stack and AGENTS conventions. |
| Architecture | Separate domain models and JPA entities with mappers | Keeps domain and application layers free of Spring/JPA annotations, consistent with hexagonal architecture. |
| ID generation | Hibernate `GenerationType.UUID`, no DB default | IDs are generated application-side; keeps schema simple and tests deterministic. |
| Timestamp type | `OffsetDateTime` normalized to UTC | Carries timezone/offset information while storing UTC internally; browser converts to local time. |
| Topic search | Trigram GIN index on `lower(title)` | Supports case-insensitive partial matches like "interview" finding "job interview about my career". |
| Topic reuse | `TopicService.ensureTopic(title)` | Repositories stay logic-free; upsert belongs in the application layer. |
| Transaction boundaries | `@Transactional` on application services/use cases | Adapters remain transaction-agnostic; lazy-loaded collections map safely within the transactional boundary. |
| Blocking vs reactive | Blocking JPA repositories | Simpler for MVP; external API calls can still use coroutines at the use-case level. |

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| Using `LIKE '%query%'` with short queries may not use the trigram index effectively | Enforce a minimum query length (e.g., 3 characters) in the application layer. |
| Bidirectional JPA entity relationships can cause mapping bugs | Map both sides in the mapper and use `CascadeType.ALL` + `orphanRemoval` on the parent. |
| Lazy-loaded feedback items fail if mapped outside a transaction | Keep `@Transactional` on use cases; optionally use `JOIN FETCH` for `findById`. |
| Topic upsert via `searchByTitle` is less direct than `findByTitle` | Acceptable for a single-user app with low topic volume; revisit if search performance becomes an issue. |

## Migration Plan

1. Merge the change.
2. Run `./gradlew flywayMigrate` (or let Spring Boot apply Flyway migrations on startup).
3. Verify tables and indexes exist in Postgres.
4. Smoke-test the `/api/analyse` endpoint and confirm rows are written to `topics`, `conversations`, and `feedback_items`.

Rollback: revert the code change. Flyway migrations are forward-only; if a rollback is needed, create a new Flyway script to drop the tables.

## Open Questions

- Where will audio files be stored? The schema only stores a reference string; the actual storage implementation is left for a follow-up change.
- Should the analysis endpoint return structured feedback items or keep returning plain text for now? This change persists structured items but keeps the API response compatible with the existing frontend.
