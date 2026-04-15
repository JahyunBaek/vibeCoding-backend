-- V18: Genomics Dashboard + Browser 메뉴 추가

INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES
    ((SELECT menu_id FROM menus WHERE name='Genomics' AND tenant_id=1 LIMIT 1),
     'Dashboard', '/genomics/dashboard', 'bar-chart-3', 0, TRUE, 'MENU', 1),
    ((SELECT menu_id FROM menus WHERE name='Genomics' AND tenant_id=1 LIMIT 1),
     'Browser', '/genomics/browser', 'dna', 6, TRUE, 'MENU', 1);

INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, r.role_key
FROM menus m
CROSS JOIN (VALUES ('ADMIN'), ('USER')) AS r(role_key)
WHERE m.tenant_id = 1 AND m.name IN ('Dashboard', 'Browser')
  AND m.path IN ('/genomics/dashboard', '/genomics/browser')
ON CONFLICT DO NOTHING;
