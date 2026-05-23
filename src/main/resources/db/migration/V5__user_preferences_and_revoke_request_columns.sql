-- Runs before Hibernate on empty DB; only alter tables that already exist (e.g. re-run / partial schema).
-- On fresh install, Hibernate ddl-auto adds these columns from entities instead.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'user_preferences'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'user_preferences' AND column_name = 'expiry_alerts'
        ) THEN
            ALTER TABLE user_preferences ADD COLUMN expiry_alerts BOOLEAN NOT NULL DEFAULT TRUE;
        END IF;
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'user_preferences' AND column_name = 'review_alerts'
        ) THEN
            ALTER TABLE user_preferences ADD COLUMN review_alerts BOOLEAN NOT NULL DEFAULT TRUE;
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'product_revoke_requests'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'product_revoke_requests' AND column_name = 'requested_by'
        ) THEN
            ALTER TABLE product_revoke_requests ADD COLUMN requested_by UUID;
        END IF;
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'product_revoke_requests' AND column_name = 'requester_email'
        ) THEN
            ALTER TABLE product_revoke_requests ADD COLUMN requester_email VARCHAR(255);
        END IF;
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = 'product_revoke_requests' AND column_name = 'requester_name'
        ) THEN
            ALTER TABLE product_revoke_requests ADD COLUMN requester_name VARCHAR(255);
        END IF;
    END IF;
END $$;
