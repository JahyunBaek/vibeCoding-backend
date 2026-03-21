SET search_path TO testdb;

-- ============================================================
-- 1. SUPER_ADMIN 전용 화면 추가
-- ============================================================
INSERT INTO screens (screen_key, screen_name, use_yn)
VALUES ('SUPER_ADMIN_TENANTS', '테넌트 관리', TRUE)
ON CONFLICT (screen_key) DO UPDATE SET screen_name = EXCLUDED.screen_name;

-- ============================================================
-- 2. SUPER_ADMIN_TENANTS 액션 추가
-- ============================================================
INSERT INTO screen_actions (screen_id, action_key, action_name, use_yn)
SELECT s.screen_id, a.action_key, a.action_name, TRUE
FROM screens s,
     (VALUES
       ('CREATE', '테넌트 추가'),
       ('EDIT',   '테넌트 수정'),
       ('DELETE', '테넌트 삭제')
     ) AS a(action_key, action_name)
WHERE s.screen_key = 'SUPER_ADMIN_TENANTS'
ON CONFLICT (screen_id, action_key) DO NOTHING;

-- ============================================================
-- 3. SUPER_ADMIN role에 SUPER_ADMIN_TENANTS 모든 액션 부여 (tenant_id=0)
-- ============================================================
INSERT INTO role_actions (tenant_id, role_key, action_id)
SELECT 0, 'SUPER_ADMIN', sa.action_id
FROM screen_actions sa
JOIN screens s ON s.screen_id = sa.screen_id
WHERE s.screen_key = 'SUPER_ADMIN_TENANTS'
ON CONFLICT DO NOTHING;
