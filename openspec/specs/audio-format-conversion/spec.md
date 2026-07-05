## ADDED Requirements

### Requirement: Non-PCM audio is converted before transcription

The system SHALL convert incoming audio to WAV PCM 16-bit 16 kHz mono before sending it to the Whisper transcription service when the audio is not already PCM-compatible.

#### Scenario: Browser WebM/Opus recording is converted

- **WHEN** the frontend submits a WebM/Opus audio recording to `POST /api/v1/conversations`
- **THEN** the backend converts the audio to WAV PCM 16-bit 16 kHz mono before calling Whisper
- **AND** Whisper receives the converted WAV file

### Requirement: PCM-compatible audio passes through unchanged

The system SHALL detect PCM-compatible audio and forward it to Whisper without re-encoding.

#### Scenario: PCM WAV recording is passed through

- **WHEN** the frontend submits a PCM WAV audio recording
- **THEN** the backend detects the PCM format
- **AND** the audio is sent to Whisper unchanged

#### Scenario: Non-PCM WAV is converted

- **WHEN** the frontend submits a WAV file encoded with a non-PCM codec
- **THEN** the backend does not treat it as PCM-compatible
- **AND** the audio is converted to WAV PCM 16-bit 16 kHz mono before transcription

### Requirement: Original uploaded audio remains unchanged

The system SHALL persist the original uploaded audio blob exactly as received, regardless of any conversion performed for transcription.

#### Scenario: WebM recording is stored in original format

- **WHEN** the frontend submits a WebM audio recording
- **THEN** the backend stores the original WebM file
- **AND** only a converted copy is used for Whisper transcription

### Requirement: Conversion failures are reported clearly

The system SHALL return a clear error response when audio conversion fails due to unsupported, corrupt, or unreadable input.

#### Scenario: Corrupt audio fails conversion

- **WHEN** the backend receives audio data that cannot be parsed or converted
- **THEN** the conversion step fails with a descriptive error
- **AND** the API returns a `400 Bad Request` with a standardized error message

### Requirement: Conversion is transparent to the API contract

The system SHALL perform audio conversion as an internal pipeline step without changing the request or response shape of `POST /api/v1/conversations`.

#### Scenario: API contract remains unchanged

- **WHEN** the frontend submits audio to `POST /api/v1/conversations`
- **THEN** the request and response fields remain identical to the existing contract
- **AND** the conversion step is not visible in the API payload
