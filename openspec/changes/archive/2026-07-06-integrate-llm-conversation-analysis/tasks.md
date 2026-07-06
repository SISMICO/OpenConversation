## 1. Domain and Port

- [x] 1.1 Create `LlmAnalysisResult` data class containing a list of `LlmFeedbackItem` (`excerpt`, `correctedExcerpt`, `explanation`) and an optional `overallComment`.
- [x] 1.2 Create `LlmAnalysisPort` interface in `application/ports/out/llm/`.
- [x] 1.3 Create `LlmAnalysisException` domain exception for LLM failures.

## 2. LLM Adapter

- [x] 2.1 Create `OpenAiCompatibleLlmAdapter` in `adapters/out/llm/` implementing `LlmAnalysisPort`.
- [x] 2.2 Build the chat-completions request with system prompt, user prompt (topic, language, transcript), model, `response_format: json_object`, and timeout.
- [x] 2.3 Parse the JSON response into `LlmAnalysisResult`; throw `LlmAnalysisException` on invalid JSON or HTTP errors.
- [x] 2.4 Omit the `Authorization` header when `apiKey` is blank; send `Bearer` token when present.

## 3. Service Integration

- [x] 3.1 Inject `LlmAnalysisPort` into `AnalyzeConversationService`.
- [x] 3.2 Replace hardcoded feedback items with a call to `LlmAnalysisPort.analyze(transcript, topicTitle, language)`.
- [x] 3.3 Map each `LlmFeedbackItem` to `FeedbackItem` with `recommendation = "Correction: ${correctedExcerpt}\n${explanation}"`.
- [x] 3.4 Ensure the transaction rolls back if LLM analysis fails (Option A).

## 4. Configuration and Warm-up

- [x] 4.1 Create `LlmProperties` configuration class with `baseUrl`, `apiKey`, `model`, and `timeoutSeconds`.
- [x] 4.2 Wire the adapter bean in `IntegrationConfig` using `LlmProperties`.
- [x] 4.3 Create async `LlmWarmupService` triggered on `ApplicationReadyEvent` that sends a trivial prompt to the configured LLM.
- [x] 4.4 Log warm-up success and failure loudly.

## 5. Error Handling

- [x] 5.1 Add `LlmAnalysisException` handler in `GlobalExceptionHandler` returning `503 Service Unavailable` with code `LLM_ANALYSIS_FAILED`.

## 6. Infrastructure

- [x] 6.1 Add `ollama` service to `infra/docker-compose.yml` with volume `ollama_data`, healthcheck, and backend `depends_on` condition.
- [x] 6.2 Create `infra/ollama-entrypoint.sh` that starts Ollama, pulls `llama3.2:3b` if missing, and keeps serving.
- [x] 6.3 Update `infra/.env.example` with `LLM_BASE_URL`, `LLM_API_KEY`, `LLM_MODEL`, and `LLM_TIMEOUT_SECONDS`.
- [x] 6.4 Update backend `application.yml` with `openconversation.llm.*` defaults pointing to the Ollama service.

## 7. Tests

- [x] 7.1 Add unit tests for `OpenAiCompatibleLlmAdapter` using a mocked `RestClient`.
- [x] 7.2 Add unit tests for `AnalyzeConversationService` verifying LLM feedback mapping and exception propagation.
- [x] 7.3 Update `AnalyzeControllerTest` if needed to assert `LLM_ANALYSIS_FAILED` error response.
- [x] 7.4 Run `./gradlew test` and `./gradlew ktlintCheck` successfully.

## 8. Verification

- [x] 8.1 Start the full Docker Compose stack and confirm Ollama pulls `llama3.2:3b` and the backend warm-up succeeds.
- [x] 8.2 Submit a test recording through the webapp and verify real feedback items are returned.
- [x] 8.3 Stop the Ollama container and confirm the backend returns `503 LLM_ANALYSIS_FAILED`.
