## ADDED Requirements

### Requirement: LLM analysis port exists
The system SHALL define an outbound port `LlmAnalysisPort` in the application layer that abstracts conversation analysis from any specific LLM provider.

#### Scenario: Service depends only on the port
- **WHEN** `AnalyzeConversationService` needs feedback
- **THEN** it calls `LlmAnalysisPort.analyze(transcript, topic, language)`
- **AND** it has no direct dependency on HTTP clients or provider APIs

### Requirement: Adapter uses OpenAI-compatible chat completions
The system SHALL provide an adapter that calls an OpenAI-compatible `/v1/chat/completions` endpoint using the configured base URL, API key, model, and timeout.

#### Scenario: Local Ollama provider
- **WHEN** `LLM_BASE_URL` is `http://ollama:11434/v1` and `LLM_API_KEY` is empty
- **THEN** the adapter calls `http://ollama:11434/v1/chat/completions` without an `Authorization` header
- **AND** it sends the configured `LLM_MODEL`

#### Scenario: External provider with API key
- **WHEN** `LLM_BASE_URL` is `https://api.external.com/v1` and `LLM_API_KEY` is set
- **THEN** the adapter calls `https://api.external.com/v1/chat/completions` with a `Bearer` token
- **AND** it sends the configured `LLM_MODEL`

### Requirement: LLM prompt requests JSON feedback
The system SHALL send a prompt that instructs the model to respond with a JSON object containing a `feedbackItems` array, where each item has `excerpt`, `correctedExcerpt`, and `explanation`.

#### Scenario: Successful analysis returns structured JSON
- **WHEN** the adapter sends the transcript and topic to the LLM
- **THEN** the model returns valid JSON matching the expected structure

### Requirement: Adapter maps LLM output to domain result
The system SHALL parse the LLM JSON response into a domain result object containing a list of feedback items with `excerpt`, `correctedExcerpt`, and `explanation`.

#### Scenario: Parse LLM response
- **WHEN** the LLM returns `{"feedbackItems": [{"excerpt": "a", "correctedExcerpt": "b", "explanation": "c"}]}`
- **THEN** the adapter returns a result containing one feedback item with those fields

### Requirement: Service joins correction and explanation into recommendation
The system SHALL map each LLM feedback item into the existing `FeedbackItem` domain object by setting `excerpt` and joining `correctedExcerpt` and `explanation` into `recommendation`.

#### Scenario: Recommendation contains correction and explanation
- **WHEN** the LLM returns an item with `correctedExcerpt` "I went" and `explanation` "Use past tense."
- **THEN** the persisted `FeedbackItem.recommendation` is "Correction: I went\nUse past tense."

### Requirement: Async warm-up loads the model after startup
The system SHALL trigger a non-blocking warm-up call to the LLM after the Spring application is ready, forcing the local model into memory without blocking incoming requests.

#### Scenario: Application starts and warm-up runs
- **WHEN** the backend application finishes starting
- **THEN** it initiates an async warm-up request to the configured LLM
- **AND** HTTP endpoints remain available during warm-up

### Requirement: LLM failures throw a domain exception
The system SHALL throw `LlmAnalysisException` when the LLM is unreachable, times out, returns an HTTP error, or returns unparseable JSON.

#### Scenario: LLM service is down
- **WHEN** the adapter cannot reach the LLM
- **THEN** it throws `LlmAnalysisException` with the underlying cause

#### Scenario: LLM returns invalid JSON
- **WHEN** the LLM response cannot be parsed into the expected JSON structure
- **THEN** the adapter throws `LlmAnalysisException` and logs the raw response
