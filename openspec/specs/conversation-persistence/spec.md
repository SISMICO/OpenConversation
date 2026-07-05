## ADDED Requirements

### Requirement: Topic is persisted with a unique title
The system SHALL persist a topic with a title and reuse an existing topic when the same title is provided again.

#### Scenario: Creating a new topic
- **WHEN** the system receives a recording with topic title "job interview about my career"
- **THEN** a new topic row is created with that title

#### Scenario: Reusing an existing topic
- **WHEN** the system receives another recording with the same topic title "job interview about my career"
- **THEN** the existing topic row is reused and no duplicate topic is created

### Requirement: Conversation is persisted with transcript and audio reference
The system SHALL persist a conversation linked to a topic, including the audio storage reference, transcript, and analysis timestamp.

#### Scenario: Persisting a successfully analyzed conversation
- **WHEN** the analysis completes for a recording
- **THEN** a conversation row is saved with the topic id, audio reference, transcript, and a non-null analyzed timestamp

### Requirement: Feedback items are persisted per conversation
The system SHALL persist each feedback item with the transcribed excerpt, recommendation, and display order.

#### Scenario: Persisting feedback items
- **WHEN** the analysis produces two feedback items for a conversation
- **THEN** both items are saved and linked to that conversation

### Requirement: Topics can be searched by partial title
The system SHALL support searching topics by a substring of the title using a case-insensitive match.

#### Scenario: Searching topics by keyword
- **WHEN** the system searches topics with query "interview"
- **THEN** the topic titled "job interview about my career" is returned

### Requirement: Conversations can be listed by topic
The system SHALL retrieve all conversations for a given topic ordered by creation date, newest first.

#### Scenario: Listing conversations for a topic
- **WHEN** the system requests conversations for a topic
- **THEN** the conversations are returned ordered by creation date from newest to oldest
