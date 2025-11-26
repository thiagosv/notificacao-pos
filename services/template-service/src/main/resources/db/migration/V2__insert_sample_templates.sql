-- Insert sample templates for demo-client

-- EMAIL template for order confirmation
INSERT INTO templates (id, client_id, channel, template_code, version, content, subject, active, created_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    'demo-client',
    'EMAIL',
    'ORDER_CONFIRMATION',
    1,
    'Olá {{customerName}}, seu pedido #{{orderId}} foi confirmado com sucesso!',
    '✅ Pedido #{{orderId}} confirmado',
    true,
    CURRENT_TIMESTAMP
);

INSERT INTO template_variables (template_id, variable_name) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'customerName'),
('550e8400-e29b-41d4-a716-446655440001', 'orderId');

-- SMS template for delivery notification
INSERT INTO templates (id, client_id, channel, template_code, version, content, subject, active, created_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440002',
    'demo-client',
    'SMS',
    'DELIVERY_NOTIFICATION',
    1,
    'Seu pedido #{{orderId}} saiu para entrega! Previsão: {{deliveryTime}}',
    NULL,
    true,
    CURRENT_TIMESTAMP
);

INSERT INTO template_variables (template_id, variable_name) VALUES
('550e8400-e29b-41d4-a716-446655440002', 'orderId'),
('550e8400-e29b-41d4-a716-446655440002', 'deliveryTime');

-- PUSH template for welcome message
INSERT INTO templates (id, client_id, channel, template_code, version, content, subject, active, created_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440003',
    'demo-client',
    'PUSH',
    'WELCOME_MESSAGE',
    1,
    'Bem-vindo(a) {{userName}}! Obrigado por se cadastrar.',
    'Bem-vindo à nossa plataforma!',
    true,
    CURRENT_TIMESTAMP
);

INSERT INTO template_variables (template_id, variable_name) VALUES
('550e8400-e29b-41d4-a716-446655440003', 'userName');

