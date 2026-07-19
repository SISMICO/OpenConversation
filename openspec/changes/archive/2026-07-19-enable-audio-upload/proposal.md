## Why

Users can already record audio directly in the browser for analysis, but they cannot reuse a recording they made elsewhere on their device. Allowing upload lets users practice with a previously saved take, share the same file across sessions, or use a recording made on another device.

## What Changes

- Add an **Upload** button to the idle-state recording controls, alongside **Start**.
- The Upload button opens the device's file picker restricted to audio files (`accept="audio/*"`).
- Once a file is selected, the webapp treats it exactly like a stopped native recording: the same **Play**, **Send**, and **Discard** controls become available, and the same backend analysis pipeline is used.
- Add an `upload(file)` action to `useAudioRecorder` that resets any prior recording, sets `blob`, `mimeType`, and `durationMs`, and transitions the recorder to the `stopped` state.
- No backend changes are required; the existing `POST /api/v1/conversations` endpoint and ffmpeg-based audio conversion already accept arbitrary audio files.
- The uploaded file is sent using the existing default filename (`recording`) so the API contract and backend storage extension behavior stay unchanged.

## Capabilities

### New Capabilities
- `audio-upload`: User can select an audio file from their device and have it handled as a native recording for playback, discard, and analysis.

### Modified Capabilities
- None. Existing recording, playback, discard, and analysis requirements do not change; upload simply provides another way to reach the same `stopped` audio state.

## Impact

- `webapp/src/hooks/useAudioRecorder.ts`: adds the `upload` action and updates the hook's interface.
- `webapp/src/App.tsx`: adds the Upload button and a hidden file input in the idle-state button group.
- `webapp/src/hooks/useAudioRecorder.test.ts` (new): unit tests for the upload action.
- `webapp/src/App.test.tsx`: adds UI integration tests for the upload control and its integration with stopped-state actions.
- No backend, API, or dependency changes.

## Testing Strategy

### Unit tests for `useAudioRecorder`

- Mock `navigator.mediaDevices.getUserMedia` and `MediaRecorder` so upload can be tested in isolation from the microphone.
- Test that calling `upload(file)` transitions the hook to `state === 'stopped'` with `blob`, `mimeType`, and `durationMs` set correctly.
- Test that `upload()` resets any active recording, previous blob, and previous error before loading the file.
- Test that `upload()` is a no-op (state stays `idle`) when called without a file.

### UI integration tests (`webapp/src/App.test.tsx`)

- **Visibility**: Upload button is rendered in `idle` state and is hidden in `recording`, `paused`, and `stopped` states.
- **Accessibility**: Upload button has a clear accessible name (`Upload` text or `aria-label="Upload"`).
- **File picker wiring**: Clicking the Upload button opens/triggers the hidden file input.
- **Selection handling**: Selecting a file through the input calls `recorder.upload(file)` with the chosen `File`.
- **State clearing**: Selecting a file clears any previous conversation result and send error.
- **Native treatment**: After upload, the Play, Send, and Discard controls are available; Send remains disabled until a topic title is provided.
- **No overlap**: While a recording is in progress, the Upload button is not shown and cannot interrupt the recording.

### Manual verification

- Select an audio file in the browser, confirm the Upload button appears in idle state and Play/Send/Discard appear after selection.
- Confirm the uploaded file can be played back and sent for analysis.
- Confirm starting a new recording clears the uploaded file and feedback, and that upload is not available during recording.
