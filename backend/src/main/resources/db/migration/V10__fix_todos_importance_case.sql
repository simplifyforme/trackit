ALTER TABLE todos
    DROP CONSTRAINT todos_importance_check;

ALTER TABLE todos
    ADD CONSTRAINT todos_importance_check
        CHECK (importance IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));
