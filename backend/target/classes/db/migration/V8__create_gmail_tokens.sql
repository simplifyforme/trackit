-- Per-user Gmail OAuth2 tokens.
-- access_token and refresh_token are AES-256-GCM encrypted at rest.
-- This table has no relationship to the app's own authentication system.
CREATE TABLE gmail_tokens (
    id            UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id       UUID         NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    access_token  TEXT         NOT NULL,
    refresh_token TEXT,
    token_type    VARCHAR(50),
    scope         VARCHAR(500),
    expires_at    TIMESTAMPTZ,
    gmail_email   VARCHAR(255),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
