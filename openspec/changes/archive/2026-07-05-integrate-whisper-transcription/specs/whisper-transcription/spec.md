## ADDED Requirements

### Requirement: Backend transcribes uploaded audio via Whisper

The system SHALL call the Whisper `/inference` endpoint with the uploaded audio bytes and return the transcribed text.

#### Scenario: Successful transcription

- **WHEN** the application service submits audio bytes to the transcription port
- **THEN** the Whisper adapter POSTs a multipart request to `${WHISPER_BASE_URL}/inference` with the audio file, `temperature=0.0`, `temperature_inc=0.2`, and `response_format=json`
- **AND** the adapter returns the value of the `text` field from the Whisper JSON response, trimmed of leading and trailing whitespace

### Requirement: Transcription failures are surfaced as domain exceptions

The system SHALL translate any Whisper HTTP error or unparseable response into a `TranscriptionFailedException`.

#### Scenario: Whisper returns an error status

- **WHEN** the Whisper endpoint returns a non-2xx HTTP status
- **THEN** the transcription port throws `TranscriptionFailedException` with a descriptive message

#### Scenario: Whisper returns an empty or invalid body

- **WHEN** the Whisper response body is missing, empty, or does not contain a `text` field
- **THEN** the transcription port throws `TranscriptionFailedException` with a descriptive message
