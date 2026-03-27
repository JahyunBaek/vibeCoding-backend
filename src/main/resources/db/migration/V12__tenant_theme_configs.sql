-- Insert default theme config keys for existing tenants
INSERT INTO tenant_configs (tenant_id, config_key, config_value)
SELECT t.tenant_id, 'primary_color', '#3B82F6'
FROM tenants t
WHERE NOT EXISTS (
  SELECT 1 FROM tenant_configs tc WHERE tc.tenant_id = t.tenant_id AND tc.config_key = 'primary_color'
);

INSERT INTO tenant_configs (tenant_id, config_key, config_value)
SELECT t.tenant_id, 'sidebar_color', '#1E293B'
FROM tenants t
WHERE NOT EXISTS (
  SELECT 1 FROM tenant_configs tc WHERE tc.tenant_id = t.tenant_id AND tc.config_key = 'sidebar_color'
);

INSERT INTO tenant_configs (tenant_id, config_key, config_value)
SELECT t.tenant_id, 'accent_color', '#2563EB'
FROM tenants t
WHERE NOT EXISTS (
  SELECT 1 FROM tenant_configs tc WHERE tc.tenant_id = t.tenant_id AND tc.config_key = 'accent_color'
);
