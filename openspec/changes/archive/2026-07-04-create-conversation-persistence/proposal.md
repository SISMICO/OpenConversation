## Why

The platform needs to persist conversations, transcripts, and feedback so users can track improvement over time. Currently the analysis flow is stateless; every recording is processed and discarded. We need a persistent backend store before we can build history, progress tracking, or reusable topics.

## What Changes

- Add PostgreSQL and Flyway dependencies to the backend.
- Create Flyway migration `V1__create_conversations_schema.sql` with `topics`, `conversations`, and `feedback_items` tables, indexes, and a trigram index for topic title search.
- Add domain models: `Topic`, `Conversation`, and `FeedbackItem` in the `domain` package.
- Add repository ports (`TopicRepositoryPort`, `ConversationRepositoryPort`) in the `application` layer.
- Add JPA entities, Spring Data repositories, repository adapters, and domain-to-entity mappers in the `adapters/out/persistence` package.
- Add `TopicService` in the application layer to handle topic upsert logic (repositories stay logic-free).
- Update the existing analysis endpoint to persist the conversation, transcript, and feedback items before returning the result.

## Capabilities

### New Capabilities

- `conversation-persistence`: Persist topics, audio references, transcripts, and per-conversation feedback items in PostgreSQL. Provide search and retrieval for topics and conversations.

### Modified Capabilities

- `mock-analysis-api`: The analysis endpoint will persist the recorded conversation and its feedback items in Postgres before returning a response. The response shape remains compatible with the existing frontend feedback display for now.

## Impact

- Backend Kotlin/Spring Boot application (new packages, dependencies, configuration).
- PostgreSQL schema (new tables and indexes managed by Flyway).
- Existing `/api/analyse` mock endpoint will now write to the database.
