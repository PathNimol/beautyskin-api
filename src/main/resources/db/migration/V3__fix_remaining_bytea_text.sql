-- Catch any remaining bytea string columns missed by V2.
DO $$
DECLARE
  r RECORD;
BEGIN
  FOR r IN
    SELECT table_name, column_name
    FROM information_schema.columns
    WHERE table_schema = 'public' AND data_type = 'bytea'
  LOOP
    EXECUTE format(
      'ALTER TABLE %I ALTER COLUMN %I TYPE text USING convert_from(%I, ''UTF8'')',
      r.table_name, r.column_name, r.column_name
    );
  END LOOP;
END $$;
