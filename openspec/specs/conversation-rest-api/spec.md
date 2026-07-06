## ADDED Requirements

### Requirement: API is versioned and resource-oriented

The system SHALL expose a versioned REST API under `/api/v1` using resource-oriented URLs for conversations and topics.

#### Scenario: API version is present in URL

- **WHEN** the frontend calls an API endpoint
- **THEN** the path starts with `/api/v1`

### Requirement: Submit audio and receive feedback

The system SHALL expose `POST /api/v1/conversations` accepting a multipart request with `audio` and `topicTitle`, store the audio locally, transcribe it via Whisper, generate feedback items via an LLM, and return the persisted conversation with transcript and feedback items.

#### Scenario: Successful audio submission

- **WHEN** the frontend POSTs a multipart request to `/api/v1/conversations` with valid `audio` and `topicTitle=job interview about my career`
- **THEN** the backend stores the audio file in the configured local directory
- **AND** the backend calls Whisper to transcribe the audio
- **AND** the backend calls the configured LLM to generate feedback items from the transcript
- **AND** each feedback item contains an `excerpt` from the transcript and a `recommendation` that includes the corrected excerpt and an explanation
- **AND** the response status is `201 Created`
- **AND** the response body contains the conversation id, topic id, topic title, transcript produced by Whisper, audio storage reference, creation timestamp, and the list of generated feedback items
- **AND** the response includes a `Location` header pointing to `/api/v1/conversations/{id}`

### Requirement: LLM analysis failures return a standardized error

The system SHALL return a standardized error when the LLM is unavailable, times out, or returns an unparseable response.

#### Scenario: LLM is unavailable

- **WHEN** the frontend POSTs a valid multipart request to `/api/v1/conversations`
- **AND** the LLM service is unavailable or returns an error
- **THEN** the response status is `503 Service Unavailable`
- **AND** the response body contains a standardized error object with code `LLM_ANALYSIS_FAILED`
- **AND** the conversation is not persisted

### Requirement: Transcription service failures return a standardized error

The system SHALL return a standardized error when Whisper is unavailable or fails to transcribe the audio.

#### Scenario: Whisper is unavailable

- **WHEN** the frontend POSTs a valid multipart request to `/api/v1/conversations`
- **AND** the Whisper service is unavailable or returns an error
- **THEN** the response status is `503 Service Unavailable`
- **AND** the response body contains a standardized error object with code `TRANSCRIPTION_FAILED`

### Requirement: List conversation history

The system SHALL expose `GET /api/v1/conversations` returning a paginated list of conversations with optional filter for `topicId`.

#### Scenario: Listing conversations with default pagination

- **WHEN** the frontend GETs `/api/v1/conversations`
- **THEN** the response status is `200 OK`
- **AND** the response body contains `data` and `pagination` objects

#### Scenario: Filtering conversations by topic

- **WHEN** the frontend GETs `/api/v1/conversations?topicId={topicId}`
- **THEN** only conversations for that topic are returned

### Requirement: Retrieve a single conversation

The system SHALL expose `GET /api/v1/conversations/{id}` returning the full conversation including transcript, feedback items, and topic title resolved from the topic relationship.

#### Scenario: Retrieving an existing conversation

- **WHEN** the frontend GETs `/api/v1/conversations/{id}` for an existing id
- **THEN** the response status is `200 OK`
- **AND** the response body contains the conversation details including the topic title

#### Scenario: Retrieving a missing conversation

- **WHEN** the frontend GETs `/api/v1/conversations/{id}` for a non-existing id
- **THEN** the response status is `404 Not Found`

### Requirement: Search topics

The system SHALL expose `GET /api/v1/topics` returning a paginated list of topics, optionally filtered by a partial title search query parameter `q`.

#### Scenario: Searching topics by keyword

- **WHEN** the frontend GETs `/api/v1/topics?q=interview`
- **THEN** topics with titles matching "interview" case-insensitively are returned
- **AND** the response includes pagination metadata

### Requirement: Retrieve feedback for a topic

The system SHALL expose `GET /api/v1/topics/{id}/feedback` returning all feedback items grouped by conversation for the given topic.

#### Scenario: Getting feedback for a topic

- **WHEN** the frontend GETs `/api/v1/topics/{id}/feedback`
- **THEN** the response status is `200 OK`
- **AND** the response body contains feedback items grouped by conversation with pagination metadata

### Requirement: Standardized error responses

The system SHALL return error responses with a consistent JSON shape containing `error.code`, `error.message`, and optional `error.details`.

#### Scenario: Validation error

- **WHEN** the frontend POSTs to `/api/v1/conversations` without a required field
- **THEN** the response status is `400 Bad Request`
- **AND** the response body contains a standardized error object

### Requirement: Deprecate old analysis endpoint

The system SHALL redirect `POST /api/analyse` to `POST /api/v1/conversations` during the migration window.

#### Scenario: Calling the old endpoint

- **WHEN** the frontend POSTs to `/api/analyse`
- **THEN** the response status is `308 Permanent Redirect`
- **AND** the `Location` header points to `/api/v1/conversations`
