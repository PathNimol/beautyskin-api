-- Some columns were created as bytea under ddl-auto; search queries use LOWER(text) and fail on bytea.
DO $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT table_name, column_name
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND data_type = 'bytea'
      AND column_name IN (
        'name', 'owner_name', 'slug', 'description', 'category', 'logo', 'logo_alt',
        'order_ref', 'customer_name', 'customer_email', 'customer_phone',
        'contact_person', 'email', 'phone', 'address', 'country',
        'first_name', 'last_name'
      )
  LOOP
    EXECUTE format(
      'ALTER TABLE %I ALTER COLUMN %I TYPE text USING convert_from(%I, ''UTF8'')',
      r.table_name, r.column_name, r.column_name
    );
  END LOOP;
END $$;
