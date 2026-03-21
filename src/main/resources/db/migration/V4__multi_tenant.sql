SET search_path TO testdb;

-- ============================================================
-- 1. tenants 테이블 생성
-- ============================================================
CREATE TABLE IF NOT EXISTS tenants (
  tenant_id   BIGSERIAL PRIMARY KEY,
  tenant_key  VARCHAR(100) UNIQUE NOT NULL,
  tenant_name VARCHAR(200) NOT NULL,
  plan_type   VARCHAR(50)  NOT NULL DEFAULT 'BASIC', -- BASIC / PRO / ENTERPRISE
  active      BOOLEAN NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 기본 테넌트 (기존 데이터용)
INSERT INTO tenants (tenant_id, tenant_key, tenant_name, plan_type, active)
VALUES (1, 'default', 'Default Tenant', 'BASIC', TRUE)
ON CONFLICT (tenant_id) DO UPDATE SET tenant_name = EXCLUDED.tenant_name;

SELECT setval(pg_get_serial_sequence('tenants','tenant_id'), (SELECT MAX(tenant_id) FROM tenants));

-- ============================================================
-- 2. SUPER_ADMIN role 추가 (시스템 레벨)
-- ============================================================
INSERT INTO roles (role_key, role_name, use_yn)
VALUES ('SUPER_ADMIN', 'Super Administrator', TRUE)
ON CONFLICT (role_key) DO UPDATE SET role_name = EXCLUDED.role_name, use_yn = EXCLUDED.use_yn;

-- ============================================================
-- 3. users 테이블에 tenant_id 추가 (SUPER_ADMIN은 NULL)
-- ============================================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL REFERENCES tenants(tenant_id);

-- 기존 사용자 → default 테넌트
UPDATE users SET tenant_id = 1 WHERE tenant_id IS NULL;

-- SUPER_ADMIN 시드 사용자 (비밀번호: superadmin1!)
INSERT INTO users (user_id, username, password_hash, name, role_key, org_id, enabled, tenant_id)
VALUES (
  3,
  'superadmin',
  '$2b$10$vwB9r1Xj08LFtfI8ni5NgOGqkbg1qo98YTXpU1aLer4Jt1f2/.TqC',
  'Super Admin',
  'SUPER_ADMIN',
  NULL,
  TRUE,
  NULL  -- SUPER_ADMIN은 테넌트 없음
)
ON CONFLICT (user_id) DO UPDATE SET
  username      = EXCLUDED.username,
  password_hash = EXCLUDED.password_hash,
  name          = EXCLUDED.name,
  role_key      = EXCLUDED.role_key,
  tenant_id     = NULL,
  enabled       = EXCLUDED.enabled,
  updated_at    = NOW();

SELECT setval(pg_get_serial_sequence('users','user_id'), (SELECT MAX(user_id) FROM users));

-- ============================================================
-- 4. orgs 테이블에 tenant_id 추가
-- ============================================================
ALTER TABLE orgs ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL REFERENCES tenants(tenant_id);
UPDATE orgs SET tenant_id = 1 WHERE tenant_id IS NULL;
ALTER TABLE orgs ALTER COLUMN tenant_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_orgs_tenant_id ON orgs(tenant_id);

-- ============================================================
-- 5. menus 테이블에 tenant_id 추가
-- ============================================================
ALTER TABLE menus ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL REFERENCES tenants(tenant_id);
UPDATE menus SET tenant_id = 1 WHERE tenant_id IS NULL;
ALTER TABLE menus ALTER COLUMN tenant_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_menus_tenant_id ON menus(tenant_id);

-- ============================================================
-- 6. boards 테이블에 tenant_id 추가
-- ============================================================
ALTER TABLE boards ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL REFERENCES tenants(tenant_id);
UPDATE boards SET tenant_id = 1 WHERE tenant_id IS NULL;
ALTER TABLE boards ALTER COLUMN tenant_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_boards_tenant_id ON boards(tenant_id);

-- ============================================================
-- 7. code_groups 에 surrogate PK + tenant_id 추가
--    (group_key는 tenant 내에서만 unique)
-- ============================================================
ALTER TABLE code_groups ADD COLUMN IF NOT EXISTS code_group_id BIGSERIAL;
ALTER TABLE code_groups ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL REFERENCES tenants(tenant_id);
UPDATE code_groups SET tenant_id = 1 WHERE tenant_id IS NULL;
ALTER TABLE code_groups ALTER COLUMN tenant_id SET NOT NULL;

-- UNIQUE 제약: tenant 내 group_key 중복 방지
CREATE UNIQUE INDEX IF NOT EXISTS uidx_code_groups_tenant_key ON code_groups(tenant_id, group_key);
CREATE INDEX IF NOT EXISTS idx_code_groups_tenant_id ON code_groups(tenant_id);

-- ============================================================
-- 8. codes 테이블에 tenant_id 추가
-- ============================================================
ALTER TABLE codes ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL REFERENCES tenants(tenant_id);
UPDATE codes SET tenant_id = 1 WHERE tenant_id IS NULL;
ALTER TABLE codes ALTER COLUMN tenant_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_codes_tenant_id ON codes(tenant_id);

-- ============================================================
-- 9. role_actions에 tenant_id 추가 (테넌트별 권한 설정)
-- ============================================================
-- 기존 PK 제거 후 재구성
ALTER TABLE role_actions ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL REFERENCES tenants(tenant_id);
UPDATE role_actions SET tenant_id = 1 WHERE tenant_id IS NULL;
ALTER TABLE role_actions ALTER COLUMN tenant_id SET NOT NULL;

-- 기존 PK(role_key, action_id) 삭제 후 (tenant_id, role_key, action_id)로 재설정
ALTER TABLE role_actions DROP CONSTRAINT IF EXISTS role_actions_pkey;
ALTER TABLE role_actions ADD PRIMARY KEY (tenant_id, role_key, action_id);
CREATE INDEX IF NOT EXISTS idx_role_actions_tenant_id ON role_actions(tenant_id);

-- ============================================================
-- 10. files 테이블에 tenant_id 추가
-- ============================================================
ALTER TABLE files ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL REFERENCES tenants(tenant_id);
UPDATE files SET tenant_id = 1 WHERE tenant_id IS NULL;
ALTER TABLE files ALTER COLUMN tenant_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_files_tenant_id ON files(tenant_id);

-- ============================================================
-- 11. SUPER_ADMIN 전용 메뉴 추가 (tenant_id 없음: system menus)
--     system 메뉴는 별도로 관리 (tenant_id=0 사용)
-- ============================================================
-- system용 tenant 생성 (SUPER_ADMIN 메뉴 소유자)
INSERT INTO tenants (tenant_id, tenant_key, tenant_name, plan_type, active)
VALUES (0, 'system', 'System', 'ENTERPRISE', TRUE)
ON CONFLICT (tenant_id) DO NOTHING;

-- tenant_id 시퀀스 재조정 (0이 들어갔으므로)
SELECT setval(pg_get_serial_sequence('tenants','tenant_id'), GREATEST((SELECT MAX(tenant_id) FROM tenants), 1));

-- SUPER_ADMIN 메뉴 (tenant_id=0: system tenant)
INSERT INTO menus (menu_id, parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES
  (100, NULL, 'System', NULL, 'globe', 0, TRUE, 'GROUP', 0),
  (101, 100, 'Tenants', '/super-admin/tenants', 'building-2', 0, TRUE, 'MENU', 0)
ON CONFLICT (menu_id) DO UPDATE SET
  name        = EXCLUDED.name,
  path        = EXCLUDED.path,
  icon        = EXCLUDED.icon,
  sort_order  = EXCLUDED.sort_order,
  use_yn      = EXCLUDED.use_yn,
  menu_type   = EXCLUDED.menu_type,
  tenant_id   = EXCLUDED.tenant_id,
  updated_at  = NOW();

SELECT setval(pg_get_serial_sequence('menus','menu_id'), GREATEST((SELECT MAX(menu_id) FROM menus), 101));

-- SUPER_ADMIN만 system 메뉴 접근
INSERT INTO menu_roles (menu_id, role_key) VALUES (100, 'SUPER_ADMIN'), (101, 'SUPER_ADMIN')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 12. SUPER_ADMIN role 에 모든 기존 action 부여 (tenant_id=0)
-- ============================================================
INSERT INTO role_actions (tenant_id, role_key, action_id)
SELECT 0, 'SUPER_ADMIN', action_id FROM screen_actions
ON CONFLICT DO NOTHING;
