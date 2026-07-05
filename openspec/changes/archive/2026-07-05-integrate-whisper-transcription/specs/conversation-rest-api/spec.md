## MODIFIED Requirements

### Requirement: Submit audio and receive feedback

The system SHALL expose `POST /api/v1/conversations` accepting a multipart request with `audio` and `topicTitle`, store the audio locally, transcribe it via Whisper, and return the persisted conversation with transcript and feedback items.

#### Scenario: Successful audio submission

- **WHEN** the frontend POSTs a multipart request to `/api/v1/conversations` with valid `audio` and `topicTitle=job interview about my career`
- **THEN** the backend stores the audio file in the configured local directory
- **AND** the backend calls Whisper to transcribe the audio
- **AND** the response status is `201 Created`
- **AND** the response body contains the conversation id, topic id, topic title, transcript produced by Whisper, audio storage reference, creation timestamp, and a list of feedback items
- **AND** the response includes a `Location` header pointing to `/api/v1/conversations/{id}`

## ADDED Requirements

### Requirement: Transcription service failures return a standardized error

The system SHALL return a standardized error when Whisper is unavailable or fails to transcribe the audio.

#### Scenario: Whisper is unavailable

- **WHEN** the frontend POSTs a valid multipart request to `/api/v1/conversations`
- **AND** the Whisper service is unavailable or returns an error
- **THEN** the response status is `503 Service Unavailable`
- **AND** the response body contains a standardized error object with code `TRANSCRIPTION_FAILED`
