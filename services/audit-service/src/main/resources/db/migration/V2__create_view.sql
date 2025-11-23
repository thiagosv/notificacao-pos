CREATE VIEW notification_summary AS
SELECT
    notification_id,
    (SELECT notification_status FROM event_store e2
     WHERE e2.notification_id = e1.notification_id
     ORDER BY timestamp DESC LIMIT 1) as current_status,
    COUNT(*) as total_events,
    MIN(timestamp) as created_at,
    MAX(timestamp) as last_update,
    SUM(CASE WHEN notification_status = 'RETRYING' THEN 1 ELSE 0 END) as retry_count
FROM event_store e1
GROUP BY notification_id;
