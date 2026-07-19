## Context

The webapp currently captures audio only through the browser's `MediaRecorder` API via `useAudioRecorder`. Once a recording is stopped, the hook exposes `blob`, `mimeType`, `durationMs`, and `state === 'stopped'`. `App.tsx` then renders Play, Send, and Discard controls, and `useAudioPlayback` handles preview. The backend accepts any audio bytes at `POST /api/v1/conversations` and normalizes them to WAV through ffmpeg before transcription.

Uploading an existing audio file from the user's device should be indistinguishable from a native recording once it reaches the stopped state.

## Goals / Non-Goals

**Goals:**
- Provide an Upload control in the same button area as Start.
- Let the user pick an audio file from their device using the native file picker.
- Load the selected file into the recorder state so that Play, Send, and Discard work exactly as they do for a recorded blob.
- Avoid backend changes by reusing the existing multipart analysis endpoint.
- Keep the API filename as the current default (`recording`) so backend storage behavior is unchanged.

**Non-Goals:**
- Computing or displaying the real duration of an uploaded file in this iteration. `durationMs` will be set to `0`; the timer will show `00:00`.
- Supporting non-audio file types.
- Uploading while a recording is in progress.
- Changing the recording format, compression, or download behavior.

## Decisions

- **Add `upload(file)` to `useAudioRecorder`.** This keeps the recorder as the single source of truth for the current audio blob and state. After `upload()` resolves, `state` is `stopped`, `blob` holds the file, and `mimeType` is `file.type` when available. All existing stopped-state UI code continues to work without branching on the audio source.
- **Use a hidden `<input type="file" accept="audio/*">` triggered by an Upload button.** The button lives in the idle-state control group next to Start, keeping the control surface consistent. Using a hidden input preserves accessibility and lets the browser present the appropriate picker on mobile and desktop.
- **Call `reset()` inside `upload()` before loading the file.** This ensures any previous recording, object URLs, streams, or timers are cleaned up, matching the behavior of starting a new recording.
- **Set `durationMs` to `0` for uploaded files.** The timer component already handles `0` by displaying `00:00`. When we later want real duration, the action can load the blob into an `HTMLAudioElement`, wait for `loadedmetadata`, and update `durationMs` without changing the rest of the design.
- **Keep the existing `createConversation` filename (`recording`).** The upload is treated as a native recording, so the API payload stays identical. The backend already derives a storage extension from the filename, and the default `.webm` fallback is acceptable for this change.
- **Do not add a new recorder state for upload loading.** The file is available synchronously from the input, so the transition to `stopped` can be immediate. If asynchronous duration loading is added later, a transient loading flag can be added without introducing a new `RecorderState` value.

## Risks / Trade-offs

- **Risk: `file.type` may be empty or inaccurate for some audio files.**  
  Mitigation: Accept any file selected via `accept="audio/*"`. The browser filter is a hint, not enforcement. The backend's ffmpeg conversion is the real validator. If future validation is needed, we can inspect bytes or extension in `upload()`.

- **Risk: Large files may hit the backend/nginx upload limit.**  
  Mitigation: The existing nginx config already allows `client_max_body_size 1G`, and the backend returns `413` for oversized payloads. We can surface that error through the existing `createConversation` error path. No new handling is required for this change.

- **Risk: Upload and recording controls could conflict.**  
  Mitigation: The Upload button is only shown in `idle` state. `upload()` calls `reset()` first, so a previous recording cannot leak into the uploaded file. Starting a new recording also calls `reset()`, so switching from an uploaded file to a live recording is safe.

- **Trade-off: Uploaded files show `00:00` duration.**  
  This keeps the first iteration small. Adding duration requires async metadata loading and test mocking of `HTMLAudioElement`, which can be done later without redesign.
