CREATE TABLE order_status_history (
    id         UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id   UUID        NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source     VARCHAR(20) NOT NULL CHECK (source IN ('manual','email','system')),
    note       TEXT
);

CREATE INDEX idx_osh_order_id ON order_status_history (order_id);
