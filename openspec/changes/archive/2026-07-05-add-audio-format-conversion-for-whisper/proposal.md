## Why

Browsers typically record audio as WebM (Opus), but OpenAI Whisper works best with WAV or MP3 inputs. Without conversion, the backend risks sending unsupported or sub-optimal payloads to Whisper, which can cause transcription failures or quality degradation. This change adds a robust audio-format conversion step before Whisper transcription while avoiding unnecessary work when the incoming audio is already PCM-compatible.

## What Changes

- Introduce an audio-format conversion step in the backend transcription pipeline.
- Accept browser-recorded audio (WebM/Opus by default) on `POST /api/v1/conversations` without changing the API contract.
- Detect PCM-compatible inputs (e.g., raw PCM WAV) and pass them through to Whisper unchanged.
- Convert non-PCM inputs to a Whisper-friendly format (WAV or MP3) using `ffmpeg`.
- Decide on the ffmpeg deployment model: reuse the existing ffmpeg binary inside the Whisper container, or run a dedicated sidecar/container for conversion.
- Add unit and integration tests for format detection, conversion, and pass-through logic.
- Update infrastructure configuration (Docker Compose / Dockerfile) if a separate conversion container is chosen.

## Capabilities

### New Capabilities

- `audio-format-conversion`: Converts incoming audio to a Whisper-compatible format (WAV/MP3) before transcription, skipping conversion when the input is already PCM-compatible.

### Modified Capabilities

- None. The `conversation-rest-api` contract remains unchanged; the conversion is an internal pipeline step. The `conversation-recording` frontend behavior also remains unchanged.

## Impact

- Backend transcription pipeline (`TranscriptionPort` and Whisper adapter).
- Audio storage and temporary file handling.
- Docker Compose infrastructure (if a separate ffmpeg container is introduced).
- Test suite for the conversion logic and transcription integration.
