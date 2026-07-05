## 1. Domain / Application Layer

- [x] 1.1 Keep `Conversation` domain model unchanged (no language column).
- [x] 1.2 Update `AnalyzeConversationUseCase` to accept `audioStorageRef` and `topicTitle` only.
- [x] 1.3 Create `ListConversationsUseCase` with pagination input (`page`, `size`) and optional `topicId` filter.
- [x] 1.4 Create `GetConversationUseCase` that returns the conversation enriched with topic title.
- [x] 1.5 Create `SearchTopicsUseCase` with pagination and query `q`.
- [x] 1.6 Create `GetTopicFeedbackUseCase` to return feedback items grouped by conversation for a topic.
- [x] 1.7 Update `ConversationRepositoryPort` with paginated query methods.
- [x] 1.8 Update `TopicRepositoryPort` with paginated search method.

## 2. Persistence Adapters

- [x] 2.1 Add paginated query methods to `ConversationJpaRepository`.
- [x] 2.2 Add paginated search method to `TopicJpaRepository`.
- [x] 2.3 Implement new port methods in `ConversationRepositoryAdapter`.
- [x] 2.4 Implement new port methods in `TopicRepositoryAdapter`.

## 3. Web Adapters

- [x] 3.1 Create `ConversationController` at `/api/v1/conversations` with:
  - `POST` (multipart: `audio`, `topicTitle`) → 201 Created with full conversation body and `Location` header.
  - `GET` with optional `topicId`, `page`, `size` → paginated list of conversation summaries.
  - `GET /{id}` → full conversation with topic title resolved from the topic relationship.
- [x] 3.2 Create `TopicController` at `/api/v1/topics` with search and topic feedback endpoints.
- [x] 3.3 Create request/response DTOs in the web adapter package.
- [x] 3.4 Add global exception handler (`@RestControllerAdvice`) returning standardized error responses.
- [x] 3.5 Add redirect/deprecation for existing `POST /api/analyse`.

## 4. Configuration

- [x] 4.1 Add multipart max-file/max-request size configuration.

## 5. Tests

- [x] 5.1 Add WebMvcTest controller tests for `ConversationController` and `TopicController`.
- [x] 5.2 Add unit tests for new use cases with MockK.
- [x] 5.3 Add integration tests for paginated repository queries.
- [x] 5.4 Update existing `AnalyzeConversationService` tests if needed.
- [x] 5.5 Run `./gradlew test` and `./gradlew build` successfully.

## 6. Verification

- [x] 6.1 Start the backend and Postgres; verify Flyway migration state is unchanged.
- [x] 6.2 Call `POST /api/v1/conversations` with multipart audio and confirm response.
- [x] 6.3 Call `GET /api/v1/conversations`, `GET /api/v1/conversations/{id}`, `GET /api/v1/topics`, and `GET /api/v1/topics/{id}/feedback`.
