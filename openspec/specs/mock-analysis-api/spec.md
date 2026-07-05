## ADDED Requirements

### Requirement: Mock endpoint accepts audio upload
The system SHALL expose a mock endpoint at `/api/analyse` on the same domain that accepts a multipart upload containing the recorded audio.

#### Scenario: Sending audio to the mock endpoint
- **WHEN** the frontend POSTs a multipart request to `/api/analyse` with an `audio` field and a `mimeType` field
- **THEN** the endpoint accepts the request and begins a simulated analysis process

### Requirement: Mock endpoint simulates processing delay
The system SHALL delay the mock response for approximately 10 seconds to simulate a slow analysis pipeline.

#### Scenario: Waiting for mock analysis
- **WHEN** the frontend sends a recording to `/api/analyse`
- **THEN** the response is not returned for approximately 10 seconds

### Requirement: Mock endpoint returns placeholder feedback
The system SHALL return a JSON response containing random placeholder feedback text.

#### Scenario: Receiving mock feedback
- **WHEN** the simulated delay completes
- **THEN** the endpoint returns a JSON payload with a `feedback` field containing placeholder text

### Requirement: Request includes language and format metadata
The system SHALL receive the fixed language marker and detected audio MIME type alongside the audio blob.

#### Scenario: Request contains metadata
- **WHEN** the frontend sends a recording to `/api/analyse`
- **THEN** the request includes a `language` field with value `en` and a `mimeType` field describing the browser’s recorded format

### Requirement: Analysis endpoint persists conversation data
The system SHALL store the topic, audio reference, transcript, and feedback items in PostgreSQL when a recording is analyzed.

#### Scenario: Successful analysis persists data
- **WHEN** the frontend sends a recording to `/api/analyse`
- **THEN** the system saves the topic, conversation, transcript, and feedback items to the database before returning a response
