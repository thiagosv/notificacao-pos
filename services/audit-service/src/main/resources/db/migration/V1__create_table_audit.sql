CREATE TABLE event_store (
    id UUID PRIMARY KEY,
    notification_id UUID NOT NULL,
    notification_status VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    version INT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_event_store_notification_id ON event_store(notification_id);
CREATE INDEX idx_event_store_status ON event_store(notification_status);