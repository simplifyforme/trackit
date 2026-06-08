-- Email confirmation tokens
CREATE TABLE email_verification_tokens (
    id         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ
);

CREATE INDEX idx_evt_token   ON email_verification_tokens (token);
CREATE INDEX idx_evt_user_id ON email_verification_tokens (user_id);

-- Password reset tokens
CREATE TABLE password_reset_tokens (
    id         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ
);

CREATE INDEX idx_prt_token   ON password_reset_tokens (token);
CREATE INDEX idx_prt_user_id ON password_reset_tokens (user_id);

-- Refresh tokens  (server-side blacklist for logout / password-change invalidation)
CREATE TABLE refresh_tokens (
    id         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_rt_token   ON refresh_tokens (token);
CREATE INDEX idx_rt_user_id ON refresh_tokens (user_id);
