## Context

The webapp already records audio via `useAudioRecorder`, plays it back via `useAudioPlayback`, and sends it to the backend for analysis. The recorder exposes the recorded `Blob` and its `mimeType`. The stopped-state UI currently shows Play, Send, and Discard controls. This change adds a Download control using only client-side APIs.

## Goals / Non-Goals

**Goals:**
- Allow the user to save the recorded audio to their local device after stopping a recording.
- Keep the download control visually grouped with the other stopped-state actions.
- Preserve existing recorder, playback, and send behavior.
- Derive the file extension from the recorded MIME type so downloaded files open correctly.

**Non-Goals:**
- Adding backend endpoints or server-side storage for downloads.
- Supporting download during recording or while analysis is in progress.
- Changing the recording format or compression settings.

## Decisions

- **Use an ephemeral `<a>` element with `download` attribute.** This avoids extra dependencies and works across modern browsers. `URL.createObjectURL(recorder.blob)` creates the object URL, the anchor triggers the download, and `URL.revokeObjectURL(url)` releases it.
- **Derive the filename extension from `recorder.mimeType`.** `audio/webm;codecs=opus` maps to `.webm`; `audio/mp4` maps to `.mp4`. A small helper handles the mapping so future MIME types are easy to add.
- **Place the Download button alongside Play, Send, and Discard in `App.tsx`.** It appears only when `recorder.state === 'stopped' && !isSending`, matching the existing stopped-state button group.
- **Disable the button when `!recorder.blob`.** This mirrors how Play and Send are gated and provides a consistent user experience.

## Risks / Trade-offs

- **Browser MIME type support varies.** The recorder already falls back from `audio/webm` to `audio/mp4`, and the download helper must map both. If a browser returns an unrecognised MIME type, the file may get a generic `.bin` extension. Mitigation: default to `.webm` for audio/webm variants and `.mp4` for audio/mp4, with `.bin` as a safe fallback.
- **Object URL cleanup.** Forgetting to revoke the URL leaks memory. Mitigation: revoke immediately after the click event in the same handler, as the browser keeps the resource alive long enough for the download to start.
- **No progress indication.** Large recordings download through the browser's own UI. This is acceptable because recordings are short practice clips.
