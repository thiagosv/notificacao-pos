INSERT INTO quotas (client_id, channel, total_quota, used_quota, available_quota, active)
VALUES
    ('demo-client', 'EMAIL', 1000, 0, 1000, true),
    ('demo-client', 'SMS', 500, 0, 500, true),
    ('demo-client', 'PUSH', 2000, 0, 2000, true),
    ('demo-client', 'WHATSAPP', 300, 0, 300, true);