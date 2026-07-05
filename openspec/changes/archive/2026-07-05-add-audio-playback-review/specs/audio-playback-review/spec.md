## ADDED Requirements

### Requirement: User can play the recorded audio
The system SHALL provide a control that plays the recorded audio from the beginning after a recording has been stopped.

#### Scenario: Playing a completed recording
- **WHEN** the user clicks the Play button in the stopped state
- **THEN** playback of the recorded audio begins and the control changes to indicate audio is playing

### Requirement: User can stop audio playback
The system SHALL provide a control that stops playback and returns the audio to the beginning.

#### Scenario: Stopping playback
- **WHEN** the user clicks the Stop button during playback
- **THEN** playback stops and the control changes back to indicate audio is ready to play

### Requirement: Playback controls are only available after recording is stopped
The system SHALL show the playback controls only when a recorded audio blob exists and the recorder is in the stopped state.

#### Scenario: Playback hidden while recording
- **WHEN** the recorder is in the recording or paused state
- **THEN** the Play button for recorded audio is not shown

#### Scenario: Playback shown after stop
- **WHEN** the user stops a recording and an audio blob is produced
- **THEN** the Play button becomes available

### Requirement: Playback state is independent from recording state
The system SHALL manage playback state separately so that starting, playing, or stopping playback does not alter the recording state.

#### Scenario: Playing does not change recording state
- **WHEN** the user plays the recorded audio
- **THEN** the recorder remains in the stopped state and the recorded blob is preserved

### Requirement: Playback resources are cleaned up
The system SHALL release object URLs and audio element resources when playback ends, the recording is discarded, or the component unmounts.

#### Scenario: Discarding a recording stops playback
- **WHEN** the user discards the recording while audio is playing
- **THEN** playback stops and the object URL for the recording is released
