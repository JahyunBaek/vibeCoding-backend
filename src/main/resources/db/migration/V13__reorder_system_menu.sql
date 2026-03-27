SET search_path TO testdb;

-- System(Tenants) 그룹을 Admin 그룹 뒤로 이동
-- 기존: System sort_order=0 (Admin보다 앞)
-- 변경: System sort_order=15 (Admin=10 뒤, My Info=20 앞)
UPDATE menus SET sort_order = 15, updated_at = NOW()
WHERE menu_id = 100 AND tenant_id = 0;
