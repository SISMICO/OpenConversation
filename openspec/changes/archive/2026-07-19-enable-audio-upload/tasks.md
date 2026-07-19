## 1. Extend the audio recorder hook with upload

- [x] 1.1 Add `upload: (file: File) => Promise<void>` to the `AudioRecorderActions` interface in `webapp/src/hooks/useAudioRecorder.ts`.
- [x] 1.2 Implement `upload` so it calls `reset()`, then sets `blob` to the file, `mimeType` to `file.type` (or `null` if empty), `durationMs` to `0`, and `state` to `'stopped'`.
- [x] 1.3 Set a generic recorder error and return to `'idle'` if `upload` throws or the file is missing.

## 2. Add the upload control to the UI

- [x] 2.1 Import the `Upload` icon from `lucide-react` into `webapp/src/App.tsx`.
- [x] 2.2 Add a hidden `<input type="file" accept="audio/*">` in `App.tsx` and store a ref to trigger it from a button.
- [x] 2.3 Add an Upload button in the `recorder.state === 'idle'` control group, alongside the Start button.
- [x] 2.4 Wire the Upload button's `onClick` to open the file input, and wire the file input's `onChange` to call `recorder.upload(file)` and then reset the input value.
- [x] 2.5 Clear `conversation` and `sendError` before upload, matching the behavior of starting a new recording.

## 3. Add unit tests for the upload action in `useAudioRecorder.test.ts`

Mock `navigator.mediaDevices.getUserMedia` to return a fake stream (`{ getTracks: () => [{ stop: vi.fn() }] }`) and `MediaRecorder` with `start`, `stop`, `pause`, `resume`, and event handlers (`ondataavailable`, `onstop`, `onerror`).

- [x] 3.1 **Transition to stopped state**: render the hook, create a `File(['audio content'], 'test.webm', { type: 'audio/webm' })`, call `result.current.upload(file)` inside `act`, and assert:
  - `state === 'stopped'`
  - `blob === file`
  - `mimeType === 'audio/webm'`
  - `durationMs === 0`
  - `error === null`
- [x] 3.2 **Reset before upload**: start a recording using the mocked `MediaRecorder`, then call `upload(file)`. Assert:
  - `MediaRecorder.prototype.stop` was called
  - stream tracks were stopped
  - the final state contains the uploaded file, not the recorded blob
  - `mimeType` reflects the uploaded file
- [x] 3.3 **Clear previous error**: trigger an error state by rejecting `getUserMedia`, then call `upload(file)` and assert `error === null`.
- [x] 3.4 **No-op without a file**: call `upload(null as unknown as File)` and assert `state` remains `'idle'`, `blob` remains `null`, and `mimeType` remains `null`.

## 4. Add UI integration tests in `App.test.tsx`

Use the existing mock pattern for `useAudioRecorder` and `useAudioPlayback`. Provide a mocked `upload` action in the recorder mock.

- [x] 4.1 **Upload button visible in idle state**: render with recorder state `idle` and assert `screen.getByRole('button', { name: 'Upload' })` exists.
- [x] 4.2 **Upload button hidden outside idle state**: render with recorder state `recording`, `paused`, and `stopped` in separate tests and assert `screen.queryByRole('button', { name: 'Upload' })` is not in the document.
- [x] 4.3 **Upload button is accessible**: assert the Upload button has accessible name `"Upload"` (via visible text or `aria-label`).
- [x] 4.4 **File input is triggered by Upload button**: render with recorder state `idle`, click the Upload button, and assert the hidden file input receives a click (e.g., by spying on `HTMLInputElement.prototype.click` or by adding a `data-testid` to the input and checking it is not disabled).
- [x] 4.5 **Selecting a file calls `recorder.upload`**: render with a mocked `upload` action, create a `File`, trigger a `change` event on the file input with `target.files` containing the file, and assert `recorder.upload` was called once with that file.
- [x] 4.6 **Upload clears previous feedback**: render with a completed conversation visible, select a file through the upload input, and assert the feedback placeholder text ("Your feedback will appear here...") is shown.
- [x] 4.7 **Upload clears previous send error**: render with `sendError` set, select a file, and assert the error alert is no longer in the document.
- [x] 4.8 **Stopped-state controls appear after upload**: render with recorder state `stopped` and a non-null blob (simulating the state after upload) and assert Play, Send, and Discard buttons are present.
- [x] 4.9 **Send disabled without topic title after upload**: render with recorder state `stopped`, blob set, and an empty `topicTitle`, and assert the Send button is disabled.
- [x] 4.10 **Start button hidden after upload**: render with recorder state `stopped` and assert the Start button is not shown.

## 5. Verify behavior

- [x] 5.1 Run the webapp tests (`npm test` in `webapp/`) and ensure existing tests still pass.
- [x] 5.2 Manually verify that selecting an audio file via the Upload button loads it, enables Play/Send/Discard, and that Send submits the file for analysis.
