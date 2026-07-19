## ADDED Requirements

### Requirement: User can upload an audio file from their device
The system SHALL provide a control that opens the device's file picker so the user can select an audio file to analyze.

#### Scenario: Upload button visible in idle state
- **WHEN** the recorder is in the idle state
- **THEN** the Upload button is shown alongside the Start recording button

#### Scenario: File picker opens on upload click
- **WHEN** the user clicks the Upload button
- **THEN** the browser opens a file picker restricted to audio files

### Requirement: Uploaded audio is treated as a native recording
The system SHALL load the selected audio file into the recorder state so that the same playback, send, and discard controls available for a recorded audio are available for an uploaded audio.

#### Scenario: Upload transitions to stopped state
- **WHEN** the user selects an audio file through the upload control
- **THEN** the recorder state becomes `stopped`, `blob` contains the file, and `mimeType` reflects the file type

#### Scenario: Uploaded audio can be played back
- **WHEN** an uploaded audio is loaded and the user clicks the Play button
- **THEN** the audio plays through the existing playback hook

#### Scenario: Uploaded audio can be sent for analysis
- **WHEN** an uploaded audio is loaded, a topic title is provided, and the user clicks Send
- **THEN** the audio is submitted to the existing analysis endpoint

#### Scenario: Uploaded audio can be discarded
- **WHEN** an uploaded audio is loaded and the user clicks Discard and confirms
- **THEN** the audio is discarded and the UI returns to the idle state

### Requirement: Upload clears any previous recording and feedback
The system SHALL reset the recorder, playback, and feedback state before loading an uploaded file.

#### Scenario: Uploading after a previous recording
- **WHEN** the user uploads a file while a previous recording or analysis result is present
- **THEN** the previous recording, playback object URL, and feedback are cleared before the uploaded file is loaded

### Requirement: Upload is only available in idle state
The system SHALL show the Upload button only when the recorder is in the idle state.

#### Scenario: Upload hidden during recording
- **WHEN** the recorder is in the recording, paused, or stopped state
- **THEN** the Upload button is not shown

### Requirement: Upload errors are surfaced clearly
The system SHALL display a clear message if the selected file cannot be loaded.

#### Scenario: File read fails
- **WHEN** the upload action encounters an error reading the selected file
- **THEN** the UI shows a generic error message and remains in the idle state
