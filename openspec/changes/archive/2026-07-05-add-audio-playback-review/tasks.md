## 1. Playback Hook

- [x] 1.1 Create `webapp/src/hooks/useAudioPlayback.ts` with `state`, `play`, `stop`, and `reset` members.
- [x] 1.2 Implement object URL creation from the recorded blob and `<audio>` element lifecycle.
- [x] 1.3 Implement event handlers for `play`, `ended`, `pause`, and `error` to normalise state.
- [x] 1.4 Ensure cleanup revokes the object URL and pauses audio on unmount, blob change, or reset.

## 2. UI Integration

- [x] 2.1 Add playback controls (Play/Stop toggle button) to `App.tsx` in the stopped state.
- [x] 2.2 Wire the playback hook to the recorded blob from `useAudioRecorder`.
- [x] 2.3 Reset playback state when the user starts a new recording or discards the current one.
- [x] 2.4 Keep Send/Discard buttons available while playback is idle or active.

## 3. Tests

- [x] 3.1 Add `webapp/src/hooks/useAudioPlayback.test.ts` covering play, stop, ended, error, and cleanup.
- [x] 3.2 Update `App.tsx` tests to verify playback controls appear after recording and trigger playback.
- [x] 3.3 Verify `npm test` passes and coverage is maintained.

## 4. Verification

- [x] 4.1 Run `npm run lint` in `webapp/` and fix any issues.
- [x] 4.2 Run `npm run build` in `webapp/` to confirm the bundle compiles.
- [x] 4.3 Manually verify recording, playback, stop, send, and discard flows in the browser.
