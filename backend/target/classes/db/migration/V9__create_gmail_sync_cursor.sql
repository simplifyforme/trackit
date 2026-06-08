-- Tracks the Gmail historyId per user so polling only fetches new messages.
CREATE TABLE gmail_sync_cursors (
    id             UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id        UUID        NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    history_id     VARCHAR(50),
    last_synced_at TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
