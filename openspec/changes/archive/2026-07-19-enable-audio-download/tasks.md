## 1. Add download utility

- [x] 1.1 Add a helper in `webapp/src/lib/utils.ts` (or a new helper file) that maps a MIME type to a file extension (`audio/webm` → `.webm`, `audio/mp4` → `.mp4`, fallback `.bin`).
- [x] 1.2 Add a `downloadBlob` helper that creates an ephemeral anchor element, sets `href` to `URL.createObjectURL(blob)`, sets the `download` attribute, triggers the click, and revokes the object URL.

## 2. Update the recording UI

- [x] 2.1 Import the `Download` icon from `lucide-react` and the download helpers into `webapp/src/App.tsx`.
- [x] 2.2 Add a Download button inside the `recorder.state === 'stopped' && !isSending` button group, disabled when `!recorder.blob`.
- [x] 2.3 Wire the Download button's `onClick` to call `downloadBlob(recorder.blob, 'recording' + extension)` using the MIME-type extension helper.

## 3. Add tests

- [x] 3.1 Add unit tests for the MIME-type extension helper covering `audio/webm`, `audio/webm;codecs=opus`, `audio/mp4`, and fallback cases.
- [x] 3.2 Add unit tests for `downloadBlob` verifying it creates an anchor with the correct `download` filename, assigns an object URL, triggers click, and revokes the URL.
- [x] 3.3 Add a unit test verifying `downloadBlob` no-ops safely when given a `null` blob.
- [x] 3.4 Add a unit test verifying repeated calls to `downloadBlob` create and revoke a fresh object URL each time.
- [x] 3.5 Add UI integration tests in `webapp/src/App.test.tsx` for Download button visibility (hidden in idle/recording/paused, visible in stopped with blob).
- [x] 3.6 Add a UI integration test asserting the Download button is disabled in the stopped state when `blob` is null.
- [x] 3.7 Add a UI integration test asserting the Download button has an accessible name (`Download`).
- [x] 3.8 Add UI integration tests asserting that clicking Download calls the download utility with the recorded blob and the correct filename for `audio/webm`, `audio/mp4`, and fallback MIME types.
- [x] 3.9 Add a UI integration test asserting that after clicking Download, the Play, Send, and Discard controls remain available and functional.
- [x] 3.10 Add a UI integration test asserting the Download button is hidden while a recording is being sent for analysis.

## 4. Verify behavior

- [x] 4.1 Run the webapp tests (`npm test` in `webapp/`) and ensure existing tests still pass.
- [x] 4.2 Manually verify that clicking Download saves the recorded audio with the correct extension and that Play/Send/Discard continue to work.
