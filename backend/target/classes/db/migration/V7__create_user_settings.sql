-- Per-user app settings.
-- openrouter_api_key is AES-256-GCM encrypted at rest; never returned in plaintext.
CREATE TABLE user_settings (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id             UUID         NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    openrouter_api_key  TEXT,
    openrouter_model    VARCHAR(255) NOT NULL DEFAULT 'openrouter/auto',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
