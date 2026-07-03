## Context

The webapp is a fresh Vite + React + TypeScript project using Tailwind CSS, shadcn/ui, React Router, React Query, axios, and sonner. The backend is still scaffolding, so the `/api/analyse` endpoint will be mocked for this change. The goal is to ship the first user-facing page: record audio in the browser, send it to a mock analyser, and display feedback.

## Goals / Non-Goals

**Goals:**

- Implement a single-page audio recording interface with Start, Pause, Resume, and Stop controls.
- Send recorded audio as `multipart/form-data` to `/api/analyse` with MIME type and fixed language metadata.
- Mock `/api/analyse` with a 10-second delay and random feedback text.
- Display a loading indicator during analysis and render the feedback text in the main area.
- Reset feedback when a new recording starts.
- Provide simple error handling for permission, device, and generic failures.
- Make the layout responsive for mobile and desktop.

**Non-Goals:**

- Real backend integration or Whisper/Ollama connectivity.
- User authentication, history, or persisted conversations.
- Editable or AI-generated topics.
- Audio playback preview after recording.
- Transcription display separate from feedback.
- Offline support or service workers.

## Decisions

### State machine for the recording UI

The UI is driven by a small explicit state machine: `idle` → `recording` ↔ `paused` → `stopped` → `sending` → `feedback`. Starting a new recording from any terminal state resets feedback and returns to `recording`. This avoids a forest of boolean flags and makes button visibility deterministic.

**Rationale**: A single state value replaces multiple booleans (`isRecording`, `isPaused`, `isSending`, etc.) and prevents invalid combinations.

### MediaRecorder with browser-default MIME type

We will use the native `MediaRecorder` API with the best supported MIME type (`audio/webm;codecs=opus` preferred, falling back to `audio/mp4`). The blob and detected MIME type are sent to the backend. Pause and Resume are true `MediaRecorder` operations; pause support is assumed for target browsers, with graceful degradation deferred.

**Rationale**: Sending the raw browser output avoids client-side transcoding and keeps the first version simple. The backend can normalise formats later. Including `mimeType` lets the backend know what it received.

### Multipart form payload

The POST to `/api/analyse` uses `multipart/form-data` with:

- `audio`: the recorded `Blob`
- `mimeType`: detected MIME type string
- `language`: fixed value `en`

**Rationale**: Multipart is the standard way to upload binary files, easy to extend later, and matches what a Spring Boot backend would naturally consume.

### Mock endpoint via Vite dev server

`/api/analyse` will be mocked locally using Vite’s dev server proxy or a small dev-only handler. The mock sleeps for 10 seconds and returns a JSON body with a randomly selected feedback string.

**Rationale**: Keeps the frontend code realistic while the backend is unavailable. Replacing the mock later requires only changing the proxy/API base URL.

### Feedback resets on new recording

When the user clicks Start, any existing feedback text is cleared immediately. This keeps the UI unambiguous: the visible feedback always belongs to the current or most recent recording session.

**Rationale**: Avoids confusion about whether displayed feedback corresponds to the current recording. History/persistence is a future concern.

### Error handling via simple toast/banner

Errors are surfaced with a brief message and reset the UI to a safe state:

- Permission denied → "Please allow microphone access to record your speech."
- No microphone → "No microphone was found on your device."
- Browser unsupported → "Your browser doesn’t support audio recording."
- Recording/API failure → "Something went wrong. Please try again."

**Rationale**: The first version does not need a sophisticated error taxonomy. These four cases cover the likely failures.

### Responsive stacked layout

Controls and topic card live in a top card; feedback occupies the main area below. On mobile, controls stack vertically and touch targets are at least 44×44 px. The feedback panel scrolls independently if content is long.

**Rationale**: Matches the user’s stated preference and naturally accommodates very long feedback text.

## Risks / Trade-offs

- **MediaRecorder MIME type varies by browser** → Mitigated by sending `mimeType` and letting the backend adapt later.
- **10-second mock delay may feel long during iterative development** → Mitigated by making the delay configurable via environment variable or easy to override.
- **No audio preview means users cannot verify their recording before sending** → Accepted trade-off for the first slice; preview can be added later.
- **Feedback text is random placeholder** → Users will not get real value yet, but the UX scaffolding will be ready.
- **Mobile browsers may pause audio recording when the screen locks** → Accepted for now; screen wake lock can be investigated later.

## Migration Plan

Not applicable for this initial feature. Future work will replace the mock endpoint with the real backend by removing the Vite proxy and pointing the API client at the Spring Boot service.

## Open Questions

- Should the mock delay be configurable for local development?
- Should we store the audio blob in React state or a ref? A ref avoids re-renders during chunk collection; state is needed for any preview later.
