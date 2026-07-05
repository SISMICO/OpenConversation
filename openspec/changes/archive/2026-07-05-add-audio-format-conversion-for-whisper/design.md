## Context

The frontend records user speech with the browser `MediaRecorder` API, which currently produces a WebM/Opus blob. The backend persists this blob and forwards it directly to the Whisper container via `POST /inference`. Whisper works most reliably with WAV or MP3 inputs, and WebM/Opus support depends on how the Whisper server was compiled and which containers are available. To make transcription robust, the backend should normalize incoming audio to a Whisper-friendly format before calling Whisper, while skipping the conversion when the input is already PCM-compatible.

## Goals / Non-Goals

**Goals:**

- Accept any common browser-recorded audio format on `POST /api/v1/conversations` without changing the API contract.
- Detect PCM-compatible audio (e.g., 16-bit PCM WAV) and pass it through to Whisper unchanged.
- Convert non-PCM audio (e.g., WebM/Opus) to WAV PCM 16-bit 16 kHz mono before Whisper transcription.
- Keep the original uploaded blob untouched so the existing playback/review flow continues to work.
- Add an `AudioConversionPort` in the application layer and a concrete ffmpeg-based adapter to preserve hexagonal architecture.
- Provide unit and integration tests for format detection, conversion, and pass-through.

**Non-Goals:**

- Changing the frontend recording implementation or the `conversation-rest-api` contract.
- Supporting real-time/streaming transcription.
- Converting persisted audio files; conversion is only for the Whisper request payload.
- Adding a cloud-hosted conversion service.

## Decisions

### 1. Run ffmpeg inside the backend container

**Decision:** Install ffmpeg in the backend Docker image and run conversion as a local subprocess.

**Rationale:**

- Keeps the conversion logic inside a single Kotlin adapter, matching the existing hexagonal architecture.
- Avoids adding a new network service, HTTP client, and operational complexity.
- The Whisper container already exposes only the `/inference` endpoint; extending it with conversion would couple conversion to the Whisper service.
- A separate ffmpeg sidecar would require defining a new HTTP API, health checks, and retry logic for little gain in local development.

**Alternatives considered:**

- *Reuse ffmpeg in the Whisper container:* Would require `docker exec`, a custom Whisper image, or a new conversion endpoint. This breaks container boundaries and complicates the Whisper service.
- *Dedicated ffmpeg microservice:* Cleaner separation but adds infrastructure, networking, and latency. Reconsider if conversion becomes CPU-isolated or shared across multiple backend instances.

### 2. Convert before transcription, not before storage

**Decision:** Store the original browser blob via `AudioStoragePort`, then convert a copy for the Whisper request.

**Rationale:**

- Preserves the original recording for playback, audit, and future reprocessing.
- The existing `audio-playback-review` capability assumes the stored file is the uploaded blob.
- Conversion settings can evolve without invalidating stored history.

### 3. Output format: WAV PCM 16-bit 16 kHz mono

**Decision:** Convert non-PCM inputs to WAV PCM 16-bit 16 kHz mono.

**Rationale:**

- WAV PCM is lossless and widely supported by Whisper.
- 16 kHz is Whisper's native sample rate, avoiding unnecessary resampling ambiguity.
- Mono reduces payload size and matches Whisper's single-channel expectation.

### 4. PCM pass-through detection

**Decision:** Detect PCM-compatible input by inspecting the WAV `fmt` chunk audio format tag (0x0001 = PCM). Non-WAV inputs are always converted.

**Rationale:**

- Fast and deterministic without spawning ffprobe for every request.
- Browser `MediaRecorder` cannot produce raw PCM WAV today, but the requirement explicitly asks for pass-through when PCM is sent.
- If a WAV file uses a non-PCM codec (e.g., WAV with Opus), it is converted.

**Future refinement:** Add MIME-type hints from the multipart request as a short-circuit, but keep magic-byte inspection as the source of truth.

### 5. Adapter placement

**Decision:** Add `AudioConversionPort` in `application/ports/out/transcription/` (or a new `audio/` package) and implement it in `adapters/out/transcription/FfmpegAudioConversionAdapter`.

**Rationale:**

- The port is an outbound dependency from the application's perspective, even though it runs in-process.
- Grouping it with transcription keeps the audio pipeline cohesive.

## Risks / Trade-offs

- **[Risk]** Installing ffmpeg increases the backend runtime image size.  
  **Mitigation:** Use Alpine's `ffmpeg` package; evaluate image size in CI and consider a separate sidecar only if the increase is unacceptable.

- **[Risk]** Spawning ffmpeg per request adds latency and CPU usage.  
  **Mitigation:** Conversion is expected to be fast for short conversational clips. Monitor durations; consider async/concurrent conversion if needed.

- **[Risk]** PCM detection by magic bytes may miss edge cases.  
  **Mitigation:** Add unit tests for common WAV PCM and non-PCM variants; fall back to conversion when uncertain.

- **[Risk]** Temporary files from ffmpeg may leak on failure.  
  **Mitigation:** Use Kotlin `use` blocks or `try/finally` to delete temp input/output files; write tests for failure paths.

## Migration Plan

1. Update the backend Dockerfile to install ffmpeg in the runtime stage.
2. Add `AudioConversionPort`, `FfmpegAudioConversionAdapter`, and `PcmAudioDetector` to the backend.
3. Wire the port into `AnalyzeConversationService` so the original blob is stored and a converted copy is transcribed.
4. Add unit tests for detection/conversion and an integration test with a real ffmpeg process.
5. Update `infra/docker-compose.yml` if any new health-check or dependency order is required (none expected for the backend-only approach).
6. Run backend tests and the full Docker Compose stack to verify end-to-end recording → transcription.

## Open Questions

- Should the converted audio also be cached/reused if the same audio is transcribed again? (Out of scope for now; original blob is authoritative.)
- Do we want to expose the converted format as a configuration property (e.g., sample rate, bit depth), or hard-code Whisper-optimal values? (Recommend hard-coding initially; make configurable if multiple transcription providers are added later.)
