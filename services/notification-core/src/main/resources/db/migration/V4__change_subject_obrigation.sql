ALTER TABLE notifications ALTER COLUMN subject DROP NOT NULL;

COMMENT ON COLUMN notifications.subject IS 'Notification subject or title (optional for SMS and PUSH)';

