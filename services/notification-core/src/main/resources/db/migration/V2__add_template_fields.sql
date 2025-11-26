ALTER TABLE notifications
ADD COLUMN template_id UUID,
ADD COLUMN template_version INTEGER;

CREATE INDEX idx_notifications_template_id ON notifications(template_id);