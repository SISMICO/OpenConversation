## ADDED Requirements

### Requirement: Theme preference is available
The system SHALL provide a theme toggle that allows the user to choose between light, dark, and system-aligned appearance.

#### Scenario: User opens the app for the first time
- **WHEN** the user loads the application
- **THEN** the theme provider initializes with the "system" setting and applies the OS preference

#### Scenario: User selects light mode
- **WHEN** the user chooses "light" from the theme toggle
- **THEN** the application switches to the light color palette and persists the choice

#### Scenario: User selects dark mode
- **WHEN** the user chooses "dark" from the theme toggle
- **THEN** the application switches to the dark color palette and persists the choice

#### Scenario: User selects system mode
- **WHEN** the user chooses "system" from the theme toggle
- **THEN** the application follows the OS preference and persists the choice

### Requirement: Theme preference is persisted
The system SHALL store the selected theme preference so it is restored on the next visit.

#### Scenario: Returning user with dark preference
- **WHEN** the user revisits the application after selecting "dark"
- **THEN** the application renders in dark mode before any flash of light mode occurs

### Requirement: Theme state is applied consistently
The system SHALL apply the active theme to all shadcn components and custom styles through a single `.dark` class mechanism.

#### Scenario: Theme changes while recording controls are visible
- **WHEN** the user changes the theme while the recording controls are displayed
- **THEN** all cards, buttons, and text update to the corresponding palette without requiring a page reload
