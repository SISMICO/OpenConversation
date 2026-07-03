## ADDED Requirements

### Requirement: User can start a recording
The system SHALL provide a control that begins audio capture from the user’s microphone using the MediaRecorder API.

#### Scenario: Starting a recording from idle
- **WHEN** the user clicks the Start recording button
- **THEN** the browser requests microphone permission, begins capturing audio, and the UI enters the recording state

### Requirement: User can pause a recording
The system SHALL provide a control that pauses an active recording without finalising it.

#### Scenario: Pausing a recording
- **WHEN** the user clicks the Pause button during an active recording
- **THEN** audio capture pauses and the UI enters the paused state

### Requirement: User can resume a paused recording
The system SHALL provide a control that resumes a paused recording and continues appending audio to the same recording.

#### Scenario: Resuming a paused recording
- **WHEN** the user clicks the Resume button while recording is paused
- **THEN** audio capture resumes and the UI returns to the recording state

### Requirement: User can stop a recording
The system SHALL provide a control that finalises the recording and produces an audio blob.

#### Scenario: Stopping a recording
- **WHEN** the user clicks the Stop button during recording or paused state
- **THEN** the recording stops, an audio Blob is produced, and the UI enters the stopped state

### Requirement: Starting a new recording clears previous feedback
The system SHALL clear any displayed feedback from a prior session when a new recording is started.

#### Scenario: Starting over after receiving feedback
- **WHEN** the user clicks the Start recording button while feedback is displayed
- **THEN** the existing feedback is removed from the screen and a new recording begins

### Requirement: User can discard a recording
The system SHALL provide a control that discards the current recording after confirmation.

#### Scenario: Discarding a recording
- **WHEN** the user clicks the Discard button and confirms the action
- **THEN** the recorded audio is discarded and the UI returns to the idle state

#### Scenario: Cancelling discard
- **WHEN** the user clicks the Discard button but cancels the confirmation
- **THEN** the recording is kept and the UI remains in the stopped state

### Requirement: Recording errors are surfaced to the user
The system SHALL display a clear message when microphone access is denied, no microphone is found, or a recording error occurs.

#### Scenario: Microphone permission denied
- **WHEN** the browser denies microphone access
- **THEN** the UI shows a message asking the user to allow microphone access

#### Scenario: No microphone available
- **WHEN** the system cannot find a microphone device
- **THEN** the UI shows a message stating that no microphone was found

#### Scenario: Recording failure
- **WHEN** an error occurs during active recording
- **THEN** the UI shows a generic error message and returns to the idle state
