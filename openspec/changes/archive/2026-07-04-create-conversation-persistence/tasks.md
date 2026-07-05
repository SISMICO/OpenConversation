## 1. Database and Dependencies

- [x] 1.1 Add `spring-boot-starter-data-jpa`, `flyway-core`, and PostgreSQL driver dependencies to `backend/build.gradle.kts`
- [x] 1.2 Configure datasource, JPA (`ddl-auto: validate`), and Flyway in `backend/src/main/resources/application.yml`
- [x] 1.3 Create `backend/src/main/resources/db/migration/V1__create_conversations_schema.sql` with `topics`, `conversations`, `feedback_items` tables, indexes, and trigram extension

## 2. Domain Models

- [x] 2.1 Create `Topic` domain model in `backend/src/main/kotlin/.../domain/topic/Topic.kt`
- [x] 2.2 Create `FeedbackItem` and `Conversation` domain models in `backend/src/main/kotlin/.../domain/conversation/`

## 3. Repository Ports

- [x] 3.1 Create `TopicRepositoryPort` interface in `backend/src/main/kotlin/.../application/ports/out/persistence/`
- [x] 3.2 Create `ConversationRepositoryPort` interface in the same package

## 4. Persistence Adapters

- [x] 4.1 Create JPA entities `TopicJpaEntity`, `ConversationJpaEntity`, and `FeedbackItemJpaEntity` in `.../adapters/out/persistence/entity/`
- [x] 4.2 Create Spring Data repositories `TopicJpaRepository` and `ConversationJpaRepository` in `.../adapters/out/persistence/repository/`
- [x] 4.3 Create domain-to-entity mappers in `.../adapters/out/persistence/mapper/`
- [x] 4.4 Create `TopicRepositoryAdapter` and `ConversationRepositoryAdapter` implementing the ports

## 5. Application Services

- [x] 5.1 Create `TopicService` with `ensureTopic(title)` upsert logic in `.../application/service/`
- [x] 5.2 Update the existing mock analysis endpoint/use case to persist the topic, conversation, transcript, and feedback items before returning the response

## 6. Tests

- [x] 6.1 Add unit tests for `TopicService`
- [x] 6.2 Add `@DataJpaTest` integration tests for repository adapters
- [x] 6.3 Add mapper tests for domain-to-entity round-trips
- [x] 6.4 Run `./gradlew build` and `./gradlew test` successfully

## 7. Verification

- [x] 7.1 Start the backend and Postgres, verify Flyway migration applies cleanly
- [x] 7.2 Call the analysis endpoint and confirm rows are written to `topics`, `conversations`, and `feedback_items`
