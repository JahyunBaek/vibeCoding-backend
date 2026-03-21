SET search_path TO testdb;

-- ============================================================
-- 1. 테넌트 설정 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS tenant_configs (
  tenant_id    BIGINT      NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
  config_key   VARCHAR(100) NOT NULL,
  config_value TEXT,
  updated_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
  PRIMARY KEY (tenant_id, config_key)
);

-- 기존 테넌트에 기본 설정값 삽입
INSERT INTO tenant_configs (tenant_id, config_key, config_value)
SELECT tenant_id, 'company_name', tenant_name FROM tenants WHERE tenant_id > 0
ON CONFLICT DO NOTHING;

INSERT INTO tenant_configs (tenant_id, config_key, config_value)
SELECT tenant_id, 'logo_url', '' FROM tenants WHERE tenant_id > 0
ON CONFLICT DO NOTHING;

INSERT INTO tenant_configs (tenant_id, config_key, config_value)
SELECT tenant_id, 'timezone', 'Asia/Seoul' FROM tenants WHERE tenant_id > 0
ON CONFLICT DO NOTHING;

INSERT INTO tenant_configs (tenant_id, config_key, config_value)
SELECT tenant_id, 'locale', 'ko' FROM tenants WHERE tenant_id > 0
ON CONFLICT DO NOTHING;

-- ============================================================
-- 2. 감사 로그 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_logs (
  log_id      BIGSERIAL    PRIMARY KEY,
  tenant_id   BIGINT,
  user_id     BIGINT,
  username    VARCHAR(100),
  action      VARCHAR(50)  NOT NULL,
  target_type VARCHAR(100),
  target_id   VARCHAR(200),
  detail      TEXT,
  ip_address  VARCHAR(45),
  created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_tenant_time ON audit_logs(tenant_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_time   ON audit_logs(user_id,   created_at DESC);

-- ============================================================
-- 3. SUPER_ADMIN 시스템 메뉴에 Settings(210) + AuditLog(211) 추가
-- ============================================================
INSERT INTO menus (menu_id, parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES
  (210, 201, 'Settings',   '/admin/settings', 'sliders',  70, TRUE, 'MENU', 0),
  (211, 201, 'Audit Log',  '/admin/audit',    'history',  80, TRUE, 'MENU', 0)
ON CONFLICT (menu_id) DO UPDATE SET
  name       = EXCLUDED.name,
  path       = EXCLUDED.path,
  icon       = EXCLUDED.icon,
  sort_order = EXCLUDED.sort_order,
  use_yn     = EXCLUDED.use_yn,
  menu_type  = EXCLUDED.menu_type,
  tenant_id  = EXCLUDED.tenant_id,
  updated_at = NOW();

SELECT setval(pg_get_serial_sequence('menus','menu_id'),
              GREATEST((SELECT MAX(menu_id) FROM menus), 211));

-- SUPER_ADMIN 에게 새 메뉴 권한 부여
INSERT INTO menu_roles (menu_id, role_key) VALUES (210, 'SUPER_ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO menu_roles (menu_id, role_key) VALUES (211, 'SUPER_ADMIN') ON CONFLICT DO NOTHING;
