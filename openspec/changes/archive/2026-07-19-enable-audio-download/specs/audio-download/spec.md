## ADDED Requirements

### Requirement: User can download the recorded audio
The system SHALL provide a control that downloads the recorded audio to the user's device after a recording has been stopped.

#### Scenario: Downloading a completed recording
- **WHEN** the user clicks the Download button in the stopped state
- **THEN** the browser initiates a download of the recorded audio Blob as a file

### Requirement: Download control is only available for a stopped recording
The system SHALL show the Download button only when the recorder is in the stopped state and a recorded audio blob exists.

#### Scenario: Download hidden while recording
- **WHEN** the recorder is in the recording or paused state
- **THEN** the Download button is not shown

#### Scenario: Download shown after stop
- **WHEN** the user stops a recording and an audio blob is produced
- **THEN** the Download button becomes available

### Requirement: Downloaded file uses a descriptive name and correct extension
The system SHALL name the downloaded file using the recording's MIME type to determine an appropriate extension (e.g., `recording.webm` for audio/webm).

#### Scenario: Downloading audio/webm recording
- **WHEN** the user downloads a recording captured as audio/webm
- **THEN** the saved file is named `recording.webm`

### Requirement: Download does not alter recording or playback state
The system SHALL preserve the recorded blob, recorder state, and playback state when the user downloads the audio.

#### Scenario: Downloading while audio is ready to play
- **WHEN** the user clicks the Download button
- **THEN** the recorded blob remains available, the recorder stays in the stopped state, and playback controls remain functional
