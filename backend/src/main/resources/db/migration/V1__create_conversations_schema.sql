CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE topics
(
    id          UUID         NOT NULL,
    title       VARCHAR(500) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_topics PRIMARY KEY (id),
    CONSTRAINT uq_topics_title UNIQUE (title)
);

CREATE INDEX idx_topics_created ON topics (created_at DESC);
CREATE INDEX idx_topics_title_lower_trgm ON topics USING GIN (lower(title) gin_trgm_ops);

CREATE TABLE conversations
(
    id                UUID         NOT NULL,
    topic_id          UUID         NOT NULL,
    audio_storage_ref VARCHAR(500) NOT NULL,
    transcript        TEXT         NOT NULL,
    analyzed_at       TIMESTAMPTZ  NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_conversations PRIMARY KEY (id),
    CONSTRAINT fk_conversations_topic FOREIGN KEY (topic_id) REFERENCES topics (id)
);

CREATE INDEX idx_conversations_topic_created ON conversations (topic_id, created_at DESC);
CREATE INDEX idx_conversations_created ON conversations (created_at DESC);

CREATE TABLE feedback_items
(
    id              UUID        NOT NULL,
    conversation_id UUID        NOT NULL,
    excerpt         TEXT        NOT NULL,
    recommendation  TEXT        NOT NULL,
    display_order   INT         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_feedback_items PRIMARY KEY (id),
    CONSTRAINT fk_feedback_items_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id)
);

CREATE INDEX idx_feedback_items_conversation_order ON feedback_items (conversation_id, display_order);
