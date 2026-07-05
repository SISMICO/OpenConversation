## Why

Conversation analysis currently uses a hardcoded simulated transcript. To provide real language-learning feedback, the backend must transcribe the uploaded audio with the running Whisper service before persisting the conversation.

## What Changes

- Introduce a `TranscriptionPort` outbound interface and a `WhisperTranscriptionAdapter` that calls the Whisper HTTP `/inference` endpoint using Spring `RestClient`.
- Introduce an `AudioStoragePort` outbound interface and a `LocalAudioStorageAdapter` that writes audio files to a Docker-mounted local directory (`./audios`). The port design keeps S3/object-storage swap possible in the future.
- Change `AnalyzeConversationUseCase.analyze(...)` to receive the raw audio bytes instead of a pre-computed storage reference.
- Update `ConversationController` to read `MultipartFile.bytes` and forward them to the use case.
- Add `TranscriptionFailedException` and map it to a `503 Service Unavailable` standardized API error.
- Mount `infra/audios` into the backend container in `infra/docker-compose.yml`.
- Add configuration for Whisper base URL and local audio storage path.
- Add unit tests for the Whisper adapter (MockWebServer) and local storage adapter.
- Update existing backend tests to account for the new use-case signature and dependencies.
- Update project documentation to describe the real transcription and local audio storage flow.

## Capabilities

### New Capabilities

- `whisper-transcription`: The backend calls the Whisper service to transcribe uploaded audio bytes into text.
- `local-audio-storage`: The backend persists uploaded audio to a local filesystem directory through an abstracted port so object storage can be substituted later.

### Modified Capabilities

- `conversation-rest-api`: `POST /api/v1/conversations` now performs real transcription via Whisper and stores the audio file locally before returning the persisted conversation. The response shape remains the same, but the transcript is produced by Whisper.

## Impact

- Backend Kotlin code (`backend/src/main/kotlin/...`) — new ports, adapters, service changes, controller changes, and exception handling.
- Backend tests (`backend/src/test/kotlin/...`) — updated service/controller tests and new adapter tests.
- Docker Compose infrastructure (`infra/docker-compose.yml`) — new `./audios` volume mount.
- Build configuration (`backend/build.gradle`) — add MockWebServer test dependency.
- Application configuration (`backend/src/main/resources/application.yml`) — new Whisper and audio storage properties.
- Documentation (`README.md`, `AGENTS.md`) — update flow description and architecture notes.
