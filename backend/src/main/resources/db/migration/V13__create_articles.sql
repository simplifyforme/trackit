CREATE TABLE articles (
    id              UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    cover_image_url TEXT,
    source_url      TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'TO_READ'
                        CHECK (status IN ('TO_READ', 'IN_PROGRESS', 'READ')),
    start_date      DATE,
    end_date        DATE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_articles_user_id ON articles (user_id);
