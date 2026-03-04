DO $$
    DECLARE
    u_name text := current_setting('app.user');
            u_pass text := current_setting('app.password');
    BEGIN
            -- 1. Create the User
            IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = u_name) THEN
                EXECUTE format('CREATE USER %I WITH PASSWORD %L', u_name, u_pass);
    END IF;

            -- 2. Setup Permissions for the Bot
    EXECUTE format('GRANT USAGE, CREATE ON SCHEMA public TO %I', u_name);
    EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO %I', u_name);
    EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO %I', u_name);
END $$;
