-- Create templates table
CREATE TABLE templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    version INTEGER NOT NULL,
    content TEXT NOT NULL,
    subject TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT unique_template_version UNIQUE (client_id, channel, template_code, version)
);

-- Create template_variables table
CREATE TABLE template_variables (
    template_id UUID NOT NULL,
    variable_name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_template FOREIGN KEY (template_id) REFERENCES templates(id) ON DELETE CASCADE,
    PRIMARY KEY (template_id, variable_name)
);

-- Create indexes
CREATE INDEX idx_template_lookup ON templates(client_id, channel, template_code, active);
CREATE INDEX idx_client_id ON templates(client_id);
CREATE INDEX idx_active_templates ON templates(active) WHERE active = TRUE;

