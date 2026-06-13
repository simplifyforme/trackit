CREATE TABLE wishlist_items (
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    product_url TEXT         NOT NULL,
    image_url   TEXT,
    notes       TEXT,
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM'
                    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    is_purchased BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wishlist_user_id ON wishlist_items (user_id);
