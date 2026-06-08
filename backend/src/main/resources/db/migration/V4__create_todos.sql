CREATE TABLE todos (
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    importance  VARCHAR(20)  NOT NULL DEFAULT 'medium'
                    CHECK (importance IN ('low', 'medium', 'high', 'critical')),
    deadline    TIMESTAMPTZ,
    is_done     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_todos_user_id       ON todos (user_id);
CREATE INDEX idx_todos_user_deadline ON todos (user_id, deadline NULLS LAST);
