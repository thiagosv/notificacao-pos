-- Create notification database and user
CREATE DATABASE notification_db;
CREATE USER notification WITH PASSWORD 'notification123';
GRANT ALL PRIVILEGES ON DATABASE notification_db TO notification;
ALTER DATABASE notification_db OWNER TO notification;

-- Connect to notification database and grant schema privileges
\c notification_db
GRANT ALL ON SCHEMA public TO notification;
GRANT ALL PRIVILEGES ON SCHEMA public TO notification;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO notification;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO notification;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO notification;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO notification;

