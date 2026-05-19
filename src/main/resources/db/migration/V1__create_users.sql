-- Core identity: one row per person who can log in.
-- Role is a column (not a separate table) — enough for admin / owner / staff / customer.

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL DEFAULT '',
    last_name     VARCHAR(100) NOT NULL DEFAULT '',
    role          VARCHAR(20)  NOT NULL,
    shop_id       VARCHAR(64),
    avatar_url    TEXT,
    phone         VARCHAR(50),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT users_email_unique UNIQUE (email),
    CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'OWNER', 'STAFF', 'CUSTOMER'))
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_shop_id ON users (shop_id) WHERE shop_id IS NOT NULL;

COMMENT ON TABLE users IS 'Platform accounts for JWT login';
COMMENT ON COLUMN users.role IS 'ADMIN=platform, OWNER=shop owner, STAFF=shop employee, CUSTOMER=buyer';
COMMENT ON COLUMN users.shop_id IS 'Set for OWNER/STAFF; null for ADMIN and CUSTOMER';
