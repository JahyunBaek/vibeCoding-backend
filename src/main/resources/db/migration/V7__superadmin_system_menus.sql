SET search_path TO testdb;

-- ============================================================
-- SUPER_ADMIN용 시스템 메뉴 (tenant_id=0) 추가
-- V4에서 System > Tenants 만 있었으므로
-- Dashboard, Admin 섹션, My Info 전체를 system tenant에 추가
-- ============================================================

INSERT INTO menus (menu_id, parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES
  -- Dashboard
  (200, NULL,  'Dashboard',      '/dashboard',     'layout-dashboard', 0,  TRUE, 'MENU',  0),

  -- Admin 섹션 (테넌트별 데이터 관리)
  (201, NULL,  'Admin',           NULL,             'settings',         10, TRUE, 'GROUP', 0),
  (202, 201,   'Common Codes',    '/admin/codes',   'code',             0,  TRUE, 'MENU',  0),
  (203, 201,   'Boards',          '/admin/boards',  'clipboard',        10, TRUE, 'MENU',  0),
  (204, 201,   'Users',           '/admin/users',   'users',            20, TRUE, 'MENU',  0),
  (205, 201,   'Orgs',            '/admin/orgs',    'building',         30, TRUE, 'MENU',  0),
  (206, 201,   'Menus',           '/admin/menus',   'menu',             40, TRUE, 'MENU',  0),
  (207, 201,   'Roles',           '/admin/roles',   'shield',           50, TRUE, 'MENU',  0),
  (208, 201,   'ScreenActions',   '/admin/screens', 'lock',             60, TRUE, 'MENU',  0),

  -- My Info
  (209, NULL,  'My Info',         '/me',            'user',             20, TRUE, 'MENU',  0)

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
              GREATEST((SELECT MAX(menu_id) FROM menus), 209));

-- SUPER_ADMIN에게 시스템 메뉴 전체 권한 부여
INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, 'SUPER_ADMIN'
  FROM menus m
 WHERE m.tenant_id = 0
ON CONFLICT DO NOTHING;
