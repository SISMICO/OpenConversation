## 1. Ports and Domain

- [x] 1.1 Create `application/ports/out/transcription/TranscriptionPort.kt` with `transcribe(audio: ByteArray, language: String?): Transcription`.
- [x] 1.2 Create `application/ports/out/storage/AudioStoragePort.kt` with `store(audio: ByteArray, originalFilename: String?): AudioStorageReference`.
- [x] 1.3 Create domain value objects `Transcription` and `AudioStorageReference`.
- [x] 1.4 Create `domain/exception/TranscriptionFailedException.kt`.

## 2. Outbound Adapters

- [x] 2.1 Create `adapters/out/transcription/WhisperTranscriptionAdapter.kt` using `RestClient` to POST multipart to `${WHISPER_BASE_URL}/inference`.
- [x] 2.2 Create `adapters/out/transcription/WhisperResponse.kt` DTO for the JSON body `{"text":"..."}`.
- [x] 2.3 Create `adapters/out/storage/LocalAudioStorageAdapter.kt` that writes to `openconversation.audio-storage.local.base-path`.
- [x] 2.4 Ensure local adapter generates UUID filenames and preserves the original extension when available.

## 3. Configuration

- [x] 3.1 Add `WhisperProperties` configuration data class bound to `openconversation.whisper`.
- [x] 3.2 Add `LocalAudioStorageProperties` configuration data class bound to `openconversation.audio-storage.local`.
- [x] 3.3 Register `WhisperTranscriptionAdapter` and `LocalAudioStorageAdapter` beans in a new or existing configuration class.
- [x] 3.4 Update `application.yml` with `openconversation.whisper.base-url` and `openconversation.audio-storage.local.base-path`.

## 4. Application Service and Controller

- [x] 4.1 Update `AnalyzeConversationUseCase.analyze(...)` signature to accept `audio: ByteArray` and `audioFilename: String?`.
- [x] 4.2 Update `AnalyzeConversationService` to inject `AudioStoragePort` and `TranscriptionPort`, orchestrate store → transcribe → persist.
- [x] 4.3 Update `ConversationController.create(...)` to read `audio.bytes` and pass bytes plus `audio.originalFilename` to the use case.
- [x] 4.4 Update `GlobalExceptionHandler` to map `TranscriptionFailedException` to `503 Service Unavailable` with code `TRANSCRIPTION_FAILED`.

## 5. Infrastructure

- [x] 5.1 Add `./audios:/app/audios` bind mount to the `backend` service in `infra/docker-compose.yml`.
- [x] 5.2 Create `infra/audios/.gitkeep` so the directory is tracked without committing audio files.
- [x] 5.3 Add `infra/audios/` to `infra/.gitignore` (if not already ignored) to prevent accidental commits of audio files.

## 6. Tests

- [x] 6.1 Add HTTP mock for adapter tests. *Note:* MockWebServer dependency could not be added to `build.gradle`, so tests use Spring `MockRestServiceServer` (already on classpath), providing equivalent coverage.
- [x] 6.2 Update `AnalyzeConversationServiceTest` to mock `AudioStoragePort` and `TranscriptionPort` and assert orchestration order.
- [x] 6.3 Update `ConversationControllerTest` for the new use-case signature.
- [x] 6.4 Add `WhisperTranscriptionAdapterTest` using `MockRestServiceServer` to assert request shape and response parsing.
- [x] 6.5 Add `LocalAudioStorageAdapterTest` using a temporary directory to assert file creation and reference format.
- [x] 6.6 Add a test for `TranscriptionFailedException` mapping in `GlobalExceptionHandlerTest`.

## 7. Documentation

- [x] 7.1 Update `README.md` to describe the real transcription flow and the `infra/audios` storage location.
- [x] 7.2 Update `AGENTS.md` architecture/flow section to mention the new `adapters/out/storage/` package and `AudioStoragePort`.
- [x] 7.3 Update backend inline comments or docs to mention the S3-ready storage port abstraction.

## 8. Verification

- [x] 8.1 Run `gradle test` in `backend/` and ensure all tests pass.
- [x] 8.2 Run `gradle ktlintCheck` in `backend/` and fix any style issues.
- [x] 8.3 Run `docker compose -f infra/docker-compose.yml up --build` and submit a recording through the webapp.
- [x] 8.4 Verify a real transcript is returned and an audio file appears in `infra/audios/`.
  - Verified with `curl` to `POST /api/v1/conversations`; response was `201 Created` with transcript `"Trying to save some audio to test my gratification."` and an audio file was written to `infra/audios/`.
  - Note: `infra/audios/` must be writable by the backend container user; added a README note about this.
