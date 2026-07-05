## Why

The webapp currently calls the mock `/api/analyse` endpoint to submit audio. This endpoint is RPC-style, unversioned, and does not expose the persisted conversations, topics, or feedback as resources. To support the recording page and future history, topic search, and topic-based feedback views, we need a versioned, resource-oriented REST API contract centered on `Conversation` and `Topic`.

## What Changes

- Introduce API versioning via URI path: `/api/v1`.
- Replace the existing `POST /api/analyse` with `POST /api/v1/conversations`.
- Add `GET /api/v1/conversations` to list conversation history with pagination and optional `topicId` filter.
- Add `GET /api/v1/conversations/{id}` to retrieve a single conversation; the topic title is resolved via the existing `topic_id` relationship.
- Add `GET /api/v1/topics` to search topics with pagination.
- Add `GET /api/v1/topics/{id}/feedback` to collect all feedback for conversations under a topic.
- Introduce a standardized error response contract.
- Extend the backend hexagonal layers with new use cases, controllers, DTOs, and pagination support.
- Update repository ports and adapters to support paginated queries.
- Deprecate `POST /api/analyse` with a redirect to the new endpoint.

## Capabilities

### New Capabilities

- `conversation-submission`: Submit audio and topic; receive transcript and structured feedback.
- `conversation-history`: List and retrieve persisted conversations.
- `topic-search`: Search topics by partial title match.
- `topic-feedback-retrieval`: Retrieve all feedback items grouped by conversation for a topic.

### Modified Capabilities

- `mock-analysis-api`: Replaced by `conversation-submission`. The old endpoint will redirect to `/api/v1/conversations` temporarily and then be removed.

## Impact

- Backend Kotlin/Spring Boot application: new controllers, DTOs, use cases, repository methods, and exception handling.
- PostgreSQL schema: no new columns required; existing `topic_id` relationship is used to resolve topic titles.
- Frontend API client: the recording page must call `/api/v1/conversations`.
- Existing `/api/analyse` consumers will be migrated via redirect.
