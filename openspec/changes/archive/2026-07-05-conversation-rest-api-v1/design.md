## Context

The backend already persists `Topic`, `Conversation`, and `FeedbackItem` via Spring Data JPA and Flyway. The existing `/api/analyse` endpoint is a thin RPC wrapper around `AnalyzeConversationUseCase`, which simulates transcription and analysis. This change transforms that endpoint into a versioned REST resource and adds the supporting history/search endpoints.

## Goals / Non-Goals

**Goals:**

- Define and implement a versioned REST API contract at `/api/v1`.
- Implement `POST /api/v1/conversations` with multipart audio upload and topic title.
- Implement paginated listing, retrieval, topic search, and topic feedback endpoints.
- Resolve topic titles for conversation responses via the existing `topic_id` relationship.
- Standardize error responses across all endpoints.
- Keep controllers thin and business logic in the application/domain layers (hexagonal architecture).

**Non-Goals:**

- Storing a `language` column on `conversations`.
- Real Whisper or LLM integration (simulated analysis remains).
- Real audio file storage beyond a reference string.
- User authentication, authorization, or multi-tenancy.
- Cursor-based pagination (offset pagination is sufficient for MVP).
- Deleting or updating conversations/topics.

## Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Versioning | URI path versioning (`/api/v1`) | Simple to implement, route, cache, and document in OpenAPI. |
| Audio upload | Multipart form-data (`audio`, `topicTitle`) | Matches the browser recording flow and the existing endpoint. |
| Resource names | Plural nouns: `conversations`, `topics` | REST convention; stable URLs. |
| IDs | UUIDs | Opaque, URL-safe, already used in the schema. |
| Pagination | Offset-based `page`/`size` with body wrapper | Straightforward for frontend and Spring Data. |
| Error shape | `{ "error": { "code", "message", "details?" } }` | Consistent and extensible. |
| Topic title resolution | Fetched via `topic_id` relationship, not stored on conversation | Avoids denormalization; leverages existing schema. |
| Topic upsert | Reuse existing `TopicService.ensureTopic` | Avoids duplicate topic rows; existing logic is preserved. |
| Deprecated endpoint | `308 Permanent Redirect` from `/api/analyse` to `/api/v1/conversations` | Gives frontend a migration window. |
| Hexagonal mapping | Controllers/DTOs in `adapters/in/web`, use cases in `application`, repositories in `adapters/out/persistence` | Keeps domain/application free of Spring/HTTP/DB concerns. |

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| Long-running analysis blocks the request | Acceptable for MVP; future work can move analysis to an async job or SSE. |
| Large audio files cause memory/timeouts | Configure multipart max file size and return `413 Payload Too Large`. |
| Topic search with short terms is slow | Enforce minimum query length (3 characters) in the application layer; trigram index exists. |
| Breaking existing frontend | Keep `/api/analyse` as a redirect until the frontend migrates. |
| N+1 topic lookups when listing conversations | Batch topic lookup by distinct `topic_id` within the use case. |

## Migration Plan

1. Merge the API change.
2. Update the frontend API client to call `/api/v1/conversations`.
3. Keep `/api/analyse` returning `308` for one release, then remove it.
4. Run `./gradlew test` and `./gradlew build`.
5. Smoke-test the new endpoints with `curl`/HTTP client.

Rollback: revert code changes. No database schema changes are required.

## Open Questions

- What is the maximum supported audio file size? (Proposed: 25 MB default, configurable.)
- What audio MIME types are supported? (Proposed: accept any audio/* for now, validate later.)
