## Why

Users currently have no way to hear what they recorded before sending it for analysis. This makes it hard to decide whether the recording is good enough or needs to be redone. Adding a quick playback control reduces wasted analysis calls and improves confidence in the recorded audio.

## What Changes

- Add an audio playback control to the webapp that becomes available after a recording is stopped.
- Provide Play and Stop buttons to listen to the recorded audio before submitting it for analysis.
- Update the recording component state machine to support a new "reviewing" state while audio is playing.
- Add unit tests covering play, stop, and the transition between recorded and playback states.

## Capabilities

### New Capabilities
- `audio-playback-review`: After a recording is stopped, the user can play and stop playback of the recorded audio to verify it before analysis.

### Modified Capabilities
<!-- No existing capability requirements are changing; playback is built on the blob already produced by conversation-recording. -->

## Impact

- Affects the React frontend (`webapp/`) only.
- Touches the audio recording component/hook and related tests.
- No backend, API, or database changes required.
