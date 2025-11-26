-- Create template database and user
CREATE DATABASE template_db;
CREATE USER template WITH PASSWORD 'template123';
GRANT ALL PRIVILEGES ON DATABASE template_db TO template;
ALTER DATABASE template_db OWNER TO template;

-- Connect to template database and grant schema privileges
\c template_db
GRANT ALL ON SCHEMA public TO template;
GRANT ALL PRIVILEGES ON SCHEMA public TO template;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO template;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO template;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO template;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO template;