SET search_path TO testdb;

-- ScreenActions 메뉴 추가 (Admin GROUP 하위, sort_order=60)
INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type)
VALUES (
  (SELECT menu_id FROM menus WHERE name = 'Admin' AND menu_type = 'GROUP'),
  'ScreenActions',
  '/admin/screens',
  'shield',
  60,
  TRUE,
  'MENU'
);

SELECT setval(pg_get_serial_sequence('menus', 'menu_id'), (SELECT MAX(menu_id) FROM menus));

-- ADMIN 역할 권한 부여
INSERT INTO menu_roles (menu_id, role_key)
SELECT menu_id, 'ADMIN' FROM menus WHERE path = '/admin/screens'
ON CONFLICT DO NOTHING;
