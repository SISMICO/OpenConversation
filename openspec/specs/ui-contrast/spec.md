## ADDED Requirements

### Requirement: App header is visible
The system SHALL display a header at the top of the page containing the application name and the theme toggle.

#### Scenario: User loads the application
- **WHEN** the application renders
- **THEN** a header is shown at the top with the text "OpenConversation" and the theme toggle

### Requirement: Primary actions use indigo accent
The system SHALL render primary action buttons with an indigo background and white text.

#### Scenario: User sees the Start button
- **WHEN** the recording is idle
- **THEN** the "Start" button is displayed with an indigo fill and clearly readable white text

#### Scenario: User sees the Send button
- **WHEN** a recording is stopped
- **THEN** the "Send" button is displayed with the same indigo fill as the Start button

### Requirement: Secondary actions are visually distinct
The system SHALL render secondary action buttons with a visible gray surface so they are recognizable as buttons against the card background.

#### Scenario: User sees the Pause button
- **WHEN** the recording is active
- **THEN** the "Pause" button is displayed with a gray surface and readable text

#### Scenario: User sees the Discard button
- **WHEN** a recording is stopped
- **THEN** the "Discard" button is displayed with a visible border or surface

### Requirement: Destructive actions use muted rose accent
The system SHALL render destructive action buttons with a muted rose tint.

#### Scenario: User sees the Stop button
- **WHEN** the recording is active or paused
- **THEN** the "Stop" button is displayed with a muted rose background and readable text

#### Scenario: User confirms discard
- **WHEN** the discard confirmation dialog is open
- **THEN** the "Discard" confirmation button uses the same muted rose treatment

### Requirement: Surfaces provide clear separation
The system SHALL use distinct background colors for the page, cards, and buttons in both light and dark themes.

#### Scenario: User views the app in dark mode
- **WHEN** the dark theme is active
- **THEN** the page background, card background, and button backgrounds have visibly different tonal values

#### Scenario: User views the app in light mode
- **WHEN** the light theme is active
- **THEN** the page background, card background, and button backgrounds have visibly different tonal values
