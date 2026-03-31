SET search_path TO testdb;

-- ============================================================
-- Analysis > Agent menu for default tenant (tenant_id=1)
-- sort_order=6: between Medical(5) and Boards(10)
-- ============================================================

INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES (NULL, 'Analysis', NULL, 'brain-circuit', 6, TRUE, 'GROUP', 1);

INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES (
  (SELECT menu_id FROM menus WHERE name='Analysis' AND tenant_id=1 AND menu_type='GROUP' LIMIT 1),
  'Agent', '/analysis/agent', 'bot', 0, TRUE, 'MENU', 1
);

-- Grant ADMIN and USER roles
INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, r.role_key
FROM menus m
CROSS JOIN (VALUES ('ADMIN'), ('USER')) AS r(role_key)
WHERE m.tenant_id = 1
  AND m.name IN ('Analysis', 'Agent')
  AND m.menu_type IN ('GROUP', 'MENU')
ON CONFLICT DO NOTHING;

-- ============================================================
-- System tenant (tenant_id=0, for SUPER_ADMIN)
-- ============================================================

INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES (NULL, 'Analysis', NULL, 'brain-circuit', 6, TRUE, 'GROUP', 0);

INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES (
  (SELECT menu_id FROM menus WHERE name='Analysis' AND tenant_id=0 AND menu_type='GROUP' LIMIT 1),
  'Agent', '/analysis/agent', 'bot', 0, TRUE, 'MENU', 0
);

INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, 'SUPER_ADMIN'
FROM menus m
WHERE m.tenant_id = 0
  AND m.name IN ('Analysis', 'Agent')
  AND m.menu_type IN ('GROUP', 'MENU')
ON CONFLICT DO NOTHING;
