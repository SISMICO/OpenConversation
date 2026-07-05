## 1. Infrastructure

- [x] 1.1 Install ffmpeg in the backend runtime Dockerfile.
- [x] 1.2 Verify the backend image builds and ffmpeg is available on PATH.

## 2. Domain and Application Ports

- [x] 2.1 Create `AudioConversionPort` interface in `application/ports/out/transcription/`.
- [x] 2.2 Define a domain exception for conversion failures (e.g., `AudioConversionException`).

## 3. Conversion Adapter

- [x] 3.1 Implement `PcmAudioDetector` that inspects WAV headers to detect PCM format (audio format tag `0x0001`).
- [x] 3.2 Implement `FfmpegAudioConversionAdapter` that:
  - Passes through PCM-compatible WAV audio unchanged.
  - Converts non-PCM audio to WAV PCM 16-bit 16 kHz mono using ffmpeg.
  - Cleans up temporary input/output files on success and failure.
- [x] 3.3 Wire the adapter into Spring configuration (`IntegrationConfig` or a new config class).

## 4. Service Integration

- [x] 4.1 Inject `AudioConversionPort` into `AnalyzeConversationService`.
- [x] 4.2 Update the analysis flow to store the original blob via `AudioStoragePort` and send a converted copy to `TranscriptionPort`.
- [x] 4.3 Map conversion failures to `400 Bad Request` via the global exception handler.

## 5. Tests

- [x] 5.1 Add unit tests for `PcmAudioDetector` covering PCM WAV, non-PCM WAV, WebM, and short/corrupt inputs.
- [x] 5.2 Add unit tests for `FfmpegAudioConversionAdapter` with mocked ffmpeg process behavior.
- [x] 5.3 Add integration tests that run real ffmpeg against sample audio fixtures.
- [x] 5.4 Update `AnalyzeConversationService` tests to verify that conversion is invoked before transcription and the original blob is stored unchanged.

## 6. Verification

- [x] 6.1 Run backend tests (`gradle test`) and lint (`gradle ktlintCheck`).
- [x] 6.2 Build and run the full Docker Compose stack.
- [x] 6.3 Record audio in the webapp and confirm successful transcription with a WebM/Opus input.
