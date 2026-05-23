CREATE TABLE IF NOT EXISTS shop_name_change_requests (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    shop_id UUID NOT NULL REFERENCES shops(id),
    current_name VARCHAR(255) NOT NULL,
    requested_name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    review_notes VARCHAR(2000),
    reviewed_by VARCHAR(255),
    requested_by UUID
);

CREATE INDEX IF NOT EXISTS idx_shop_name_change_shop_status
    ON shop_name_change_requests (shop_id, status)
    WHERE deleted = FALSE;
