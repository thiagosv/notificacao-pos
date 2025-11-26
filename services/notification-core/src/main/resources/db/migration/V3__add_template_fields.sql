ALTER TABLE notifications
ADD COLUMN template_code VARCHAR(100);

CREATE INDEX idx_notifications_template_code ON notifications(template_code);