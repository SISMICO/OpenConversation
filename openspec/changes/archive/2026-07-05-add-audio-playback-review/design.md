## Context

The webapp already records audio through `useAudioRecorder` and exposes `blob` once the recorder reaches the `stopped` state. `App.tsx` renders Send and Discard controls in that state, but there is no way for the user to hear the recording before sending it. This change introduces a playback control for the recorded blob.

## Goals / Non-Goals

**Goals:**
- Allow the user to play and stop playback of the recorded audio after recording is stopped.
- Keep the recording and playback state machines separate and composable.
- Clean up object URLs and audio elements to avoid memory leaks.
- Provide unit tests for playback behavior.

**Non-Goals:**
- Seeking/scrubbing through the recording.
- Displaying a waveform or audio visualisation.
- Persisting playback progress across recordings.
- Changing the backend API or analysis flow.

## Decisions

1. **Introduce a dedicated `useAudioPlayback` hook**
   - Rationale: Playback concerns (audio element lifecycle, play/pause events, current time) differ from recording concerns (microphone stream, MediaRecorder). A separate hook keeps `useAudioRecorder` focused and makes both easier to test.
   - Rejected alternative: adding playback methods directly to `useAudioRecorder`, which would mix unrelated responsibilities.

2. **Use `URL.createObjectURL` with an in-memory `<audio>` element**
   - Rationale: The recorded blob is already in memory; creating an object URL is the simplest way to feed it to the browser's native audio player without extra dependencies.
   - Cleanup: revoke the object URL and pause the audio when the hook unmounts, the blob changes, or `reset` is called.

3. **Keep playback UI inside `App.tsx` stopped-state block**
   - Rationale: Playback is only meaningful after recording is complete. Placing it next to Send/Discard keeps the decision flow natural: review, then send or discard.

4. **Add a single Play/Stop toggle button instead of separate Play/Pause**
   - Rationale: The user only needs to verify the recording quickly. Stop resets playback to the beginning, which is simpler than pause/resume for a short review workflow.

5. **Track playback state locally in the hook (`idle` | `playing`)**
   - Rationale: The recorder's `stopped` state should not be overloaded with playback details. A local playback state lets the UI show the correct label/icon without affecting the recorder state machine.

## Risks / Trade-offs

- [Risk] Object URLs are not revoked if cleanup is missed, causing memory leaks. → Mitigation: centralise URL creation and revocation in the playback hook and cover cleanup in tests.
- [Risk] Browsers block autoplay or may fail to decode the recorded MIME type. → Mitigation: rely on the same MIME type selected during recording; surface playback errors silently by returning to the idle state so the user can still send the recording.
- [Risk] Playback state becomes inconsistent if the audio element fires unexpected events. → Mitigation: bind to `ended`, `pause`, and `error` events and normalise state inside the hook.
