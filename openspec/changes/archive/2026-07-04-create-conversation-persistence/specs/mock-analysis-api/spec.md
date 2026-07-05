## ADDED Requirements

### Requirement: Analysis endpoint persists conversation data
The system SHALL store the topic, audio reference, transcript, and feedback items in PostgreSQL when a recording is analyzed.

#### Scenario: Successful analysis persists data
- **WHEN** the frontend sends a recording to `/api/analyse`
- **THEN** the system saves the topic, conversation, transcript, and feedback items to the database before returning a response
