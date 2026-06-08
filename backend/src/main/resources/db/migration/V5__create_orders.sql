-- status values: pending, confirmed, shipped, out_for_delivery, delivered, cancelled, returned, needs_review
-- source values: manual, email
CREATE TABLE orders (
    id               UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id          UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title            VARCHAR(255)   NOT NULL,
    description      TEXT,
    merchant         VARCHAR(255),
    amount           NUMERIC(12, 2),
    currency         VARCHAR(10),
    status           VARCHAR(30)    NOT NULL DEFAULT 'pending'
                         CHECK (status IN ('pending','confirmed','shipped','out_for_delivery',
                                           'delivered','cancelled','returned','needs_review')),
    source           VARCHAR(20)    NOT NULL DEFAULT 'manual'
                         CHECK (source IN ('manual','email')),
    external_ref     VARCHAR(255),
    order_date       TIMESTAMPTZ,
    gmail_message_id VARCHAR(255),
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_user_id     ON orders (user_id);
CREATE INDEX idx_orders_user_status ON orders (user_id, status);
-- Prevents the same Gmail message from creating duplicate orders per user
CREATE UNIQUE INDEX idx_orders_user_gmail_msg
    ON orders (user_id, gmail_message_id)
    WHERE gmail_message_id IS NOT NULL;
