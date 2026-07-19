## Why

Users currently can record, play back, and send audio for feedback, but they have no way to keep a local copy of their practice recordings. Adding a download option lets users archive their progress, share recordings outside the app, and review them later.

## What Changes

- Add a **Download** button to the recording controls that appears after a recording has been stopped and an audio blob exists.
- Clicking the button saves the recorded audio to the user's device as a file (e.g., `recording.webm` or the recorded MIME type's extension).
- The download control is disabled while no recording is available and is hidden while a recording is being sent for analysis.
- No existing recording, playback, or send behavior is changed.

## Capabilities

### New Capabilities
- `audio-download`: User can download the recorded audio after stopping the recording.

### Modified Capabilities
- None. This change introduces a new UI control; it does not alter existing requirements for recording, playback, or conversation submission.

## Impact

- `webapp/src/App.tsx`: adds the download control to the stopped-state button group.
- `webapp/src/hooks/useAudioRecorder.ts`: may need to expose the recorded MIME type or filename if not already available (the existing `blob` already provides the data).
- Browser download APIs (`URL.createObjectURL` + anchor download) — no new backend or API dependencies.

## Testing Strategy

### Unit tests for download utilities
- Test the MIME-type-to-extension helper covers `audio/webm`, `audio/webm;codecs=opus`, `audio/mp4`, and an unknown fallback.
- Test `downloadBlob` creates an anchor with `download` set to the provided filename, assigns an object URL, triggers click, and revokes the URL.
- Test `downloadBlob` no-ops safely when given a `null` blob.
- Test repeated calls to `downloadBlob` create and revoke a fresh object URL each time.

### UI integration tests (`webapp/src/App.test.tsx`)
- **Visibility**: Download button is not rendered in `idle`, `recording`, or `paused` states.
- **Availability**: Download button is rendered in the `stopped` state when a blob exists.
- **Disabled state**: Download button is disabled in the `stopped` state when `blob` is null.
- **Accessible name**: Download button has a clear accessible name (`Download` text or `aria-label="Download"`).
- **Interaction**: Clicking Download calls the download utility with the recorded blob and a filename derived from `mimeType` (e.g., `recording.webm`, `recording.mp4`, `recording.bin`).
- **State preservation**: After clicking Download, the Play, Send, and Discard controls remain available and functional; recorder and playback state are not reset.
- **Sending state**: Download button is hidden while the recording is being sent for analysis (`isSending` true).

### Manual verification
- Record audio in the browser, stop, click Download, and confirm the saved file has the expected extension and plays back.
