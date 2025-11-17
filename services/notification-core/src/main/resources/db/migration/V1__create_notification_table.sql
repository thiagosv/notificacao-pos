CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(36) PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    provider_id VARCHAR(100),
    provider_message_id VARCHAR(255),
    failure_reason TEXT,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    sent_at TIMESTAMP,
    failed_at TIMESTAMP
);

COMMENT ON TABLE notifications IS 'Stores all notification requests and their lifecycle status';
COMMENT ON COLUMN notifications.id IS 'Unique notification identifier (UUID)';
COMMENT ON COLUMN notifications.client_id IS 'Client identifier who requested the notification';
COMMENT ON COLUMN notifications.idempotency_key IS 'Client-provided key to prevent duplicate notifications';
COMMENT ON COLUMN notifications.channel IS 'Notification channel: EMAIL, SMS, PUSH, WHATSAPP';
COMMENT ON COLUMN notifications.recipient IS 'Recipient identifier (email, phone, device ID)';
COMMENT ON COLUMN notifications.subject IS 'Notification subject or title';
COMMENT ON COLUMN notifications.content IS 'Notification content or body';
COMMENT ON COLUMN notifications.status IS 'Current status: PENDING, PROCESSING, SENT, FAILED, RETRYING';
COMMENT ON COLUMN notifications.priority IS 'Notification priority: LOW, MEDIUM, HIGH, URGENT';
COMMENT ON COLUMN notifications.provider_id IS 'Provider service that processed the notification';
COMMENT ON COLUMN notifications.provider_message_id IS 'External provider message identifier';
COMMENT ON COLUMN notifications.failure_reason IS 'Reason for failure if status is FAILED';
COMMENT ON COLUMN notifications.retry_count IS 'Number of retry attempts made';
COMMENT ON COLUMN notifications.max_retries IS 'Maximum number of retry attempts allowed';
COMMENT ON COLUMN notifications.created_at IS 'Timestamp when notification was created';
COMMENT ON COLUMN notifications.updated_at IS 'Timestamp when notification was last updated';
COMMENT ON COLUMN notifications.sent_at IS 'Timestamp when notification was successfully sent';
COMMENT ON COLUMN notifications.failed_at IS 'Timestamp when notification permanently failed';

CREATE INDEX IF NOT EXISTS idx_notification_client_id ON notifications(client_id);
CREATE INDEX IF NOT EXISTS idx_notification_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notification_channel ON notifications(channel);
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON notifications(created_at);

CREATE INDEX IF NOT EXISTS idx_notification_client_status ON notifications(client_id, status);
CREATE INDEX IF NOT EXISTS idx_notification_client_channel ON notifications(client_id, channel);
CREATE INDEX IF NOT EXISTS idx_notification_status_retry ON notifications(status, retry_count);
