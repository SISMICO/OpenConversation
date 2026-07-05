## ADDED Requirements

### Requirement: Backend persists uploaded audio to local filesystem

The system SHALL store uploaded audio bytes on the local filesystem inside a configured base directory and return a stable storage reference.

#### Scenario: Successful local storage

- **WHEN** the application service submits audio bytes and an original filename to the audio storage port
- **THEN** the local adapter generates a UUID filename, preserving the original extension when available
- **AND** the adapter writes the bytes under the configured base path
- **AND** the adapter returns a reference in the form `local:///app/audios/<uuid>.<ext>`

### Requirement: Local storage directory is created on demand

The system SHALL create missing parent directories before writing an audio file.

#### Scenario: Directory does not exist

- **WHEN** the local adapter stores audio and the base directory does not yet exist
- **THEN** the adapter creates the base directory and any required parent directories
- **AND** the file is written successfully

### Requirement: Audio storage port is abstracted for future object storage

The system SHALL define `AudioStoragePort` as an interface so that a different storage backend can be substituted without changing application or domain code.

#### Scenario: Swapping storage implementation

- **WHEN** a new adapter implements `AudioStoragePort`
- **THEN** the application service continues to work without modification
