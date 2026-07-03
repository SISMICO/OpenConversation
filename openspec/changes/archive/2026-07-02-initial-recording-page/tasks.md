## 1. Project Setup

- [x] 1.1 Remove the default Vite starter content from `App.tsx` and `App.css`
- [x] 1.2 Verify Tailwind and shadcn/ui imports are available for the new page

## 2. Audio Recording Hook

- [x] 2.1 Create a `useAudioRecorder` hook that manages the MediaRecorder lifecycle
- [x] 2.2 Implement `start`, `pause`, `resume`, and `stop` actions with explicit state (`idle`, `recording`, `paused`, `stopped`)
- [x] 2.3 Collect audio chunks into a Blob using the best supported MIME type (`audio/webm;codecs=opus` preferred, fallback to `audio/mp4`)
- [x] 2.4 Expose the recorded Blob, detected MIME type, recording duration, and current state
- [x] 2.5 Handle MediaRecorder errors and surface them through the hook API

## 3. Analysis API Client and Mock

- [x] 3.1 Create an API function that POSTs the audio Blob as `multipart/form-data` to `/api/analyse` with fields `audio`, `mimeType`, and `language`
- [x] 3.2 Configure the Vite dev server to proxy or handle `/api/analyse` locally
- [x] 3.3 Implement the mock handler to sleep for 10 seconds and return a JSON response with a random `feedback` string
- [x] 3.4 Add TypeScript types for the analysis request and response

## 4. Recording Page UI

- [x] 4.1 Create a static non-editable topic card component with placeholder text
- [x] 4.2 Build the recording controls section with Start, Pause, Resume, and Stop buttons, showing only the valid actions for the current state
- [x] 4.3 Add a timer display that shows the current recording duration
- [x] 4.4 Show Send and Discard buttons after stopping, with a confirmation dialog on Discard
- [x] 4.5 Implement the stacked responsive layout: controls/topic on top, feedback area below
- [x] 4.6 Ensure touch targets are at least 44×44 px and the layout adapts to mobile viewports

## 5. Feedback and Loading States

- [x] 5.1 Display a loading indicator while the analysis request is in flight
- [x] 5.2 Render the feedback text returned by `/api/analyse` in the main content area
- [x] 5.3 Clear the feedback area when the user starts a new recording
- [x] 5.4 Disable or hide Send/Discard controls during the loading state

## 6. Error Handling

- [x] 6.1 Detect microphone permission denial and show a specific message
- [x] 6.2 Detect missing microphone and show a specific message
- [x] 6.3 Detect unsupported browsers and show a specific message
- [x] 6.4 Show a generic error message for recording failures and API failures, then reset to a safe state

## 7. Verification

- [x] 7.1 Run `npm run lint` and fix any issues
- [x] 7.2 Run `npm run build` and confirm the production build succeeds
- [x] 7.3 Manually test the full flow in Chrome/Firefox and a mobile viewport: start, pause, resume, stop, send, receive feedback, discard, and error states
- [x] 7.4 Verify the mock endpoint delay is approximately 10 seconds and the loading indicator is visible during that time
