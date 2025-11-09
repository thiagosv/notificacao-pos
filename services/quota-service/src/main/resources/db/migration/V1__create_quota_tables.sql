CREATE TABLE IF NOT EXISTS quotas (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    total_quota BIGINT NOT NULL DEFAULT 0,
    used_quota BIGINT NOT NULL DEFAULT 0,
    available_quota BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uq_client_channel UNIQUE (client_id, channel)
);

CREATE INDEX idx_quotas_client_id ON quotas(client_id);
CREATE INDEX idx_quotas_client_channel ON quotas(client_id, channel);
CREATE INDEX idx_quotas_active ON quotas(active);

CREATE TABLE IF NOT EXISTS quota_usage (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    notification_id VARCHAR(100),
    amount BIGINT NOT NULL,
    operation VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_quota_usage_client_id ON quota_usage(client_id);
CREATE INDEX idx_quota_usage_client_channel ON quota_usage(client_id, channel);
CREATE INDEX idx_quota_usage_notification_id ON quota_usage(notification_id);
CREATE INDEX idx_quota_usage_created_at ON quota_usage(created_at);

COMMENT ON TABLE quotas IS 'Stores quota configuration per client and channel';
COMMENT ON TABLE quota_usage IS 'Audit trail of quota consumption and releases';

COMMENT ON COLUMN quotas.total_quota IS 'Total quota allocated';
COMMENT ON COLUMN quotas.used_quota IS 'Amount of quota consumed';
COMMENT ON COLUMN quotas.available_quota IS 'Remaining quota available';
COMMENT ON COLUMN quota_usage.operation IS 'CONSUME or RELEASE';