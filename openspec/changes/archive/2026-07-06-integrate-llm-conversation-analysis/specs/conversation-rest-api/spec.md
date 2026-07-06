## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: LLM analysis failures return a standardized error

The system SHALL return a standardized error when the LLM is unavailable, times out, or returns an unparseable response.

#### Scenario: LLM is unavailable

- **WHEN** the frontend POSTs a valid multipart request to `/api/v1/conversations`
- **AND** the LLM service is unavailable or returns an error
- **THEN** the response status is `503 Service Unavailable`
- **AND** the response body contains a standardized error object with code `LLM_ANALYSIS_FAILED`
- **AND** the conversation is not persisted
