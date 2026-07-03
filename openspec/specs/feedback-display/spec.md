## ADDED Requirements

### Requirement: Page displays a static topic card
The system SHALL display a non-editable topic card above the recording controls.

#### Scenario: Viewing the recording page
- **WHEN** the user opens the recording page
- **THEN** a topic card is visible with static placeholder text

### Requirement: Feedback is shown in the main content area
The system SHALL render analysis feedback text returned by the analysis endpoint in the primary area of the page.

#### Scenario: Feedback received
- **WHEN** the analysis endpoint returns feedback text
- **THEN** the text is displayed in the main feedback area below the recording controls

### Requirement: Feedback is cleared on new recording
The system SHALL remove feedback text from the main content area when a new recording begins.

#### Scenario: Starting a new recording with existing feedback
- **WHEN** the user starts a new recording while feedback is displayed
- **THEN** the feedback area is emptied before the new recording state is shown

### Requirement: Loading state is shown during analysis
The system SHALL display a loading indicator while the analysis request is in progress.

#### Scenario: Sending recording for analysis
- **WHEN** the user clicks Send and the request is in flight
- **THEN** a loading indicator is visible and the Send/Discard controls are disabled or hidden

### Requirement: Page layout is responsive
The system SHALL adapt the layout for mobile and desktop viewports.

#### Scenario: Viewing on a mobile device
- **WHEN** the page is rendered on a mobile viewport
- **THEN** controls are stacked, touch targets are large enough, and the feedback area remains readable
