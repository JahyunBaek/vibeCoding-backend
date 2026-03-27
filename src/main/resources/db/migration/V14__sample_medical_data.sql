SET search_path TO testdb;

-- ============================================================
-- 1. Common Codes for Medical Samples (tenant_id = 1, default tenant)
-- ============================================================

-- Patient Status
INSERT INTO code_groups (group_key, group_name, use_yn, tenant_id)
VALUES ('PATIENT_STATUS', '환자 상태', TRUE, 1)
ON CONFLICT DO NOTHING;

INSERT INTO codes (group_key, code, name, value, sort_order, use_yn, tenant_id) VALUES
  ('PATIENT_STATUS', 'ACTIVE',     '활성',   'ACTIVE',     0, TRUE, 1),
  ('PATIENT_STATUS', 'DISCHARGED', '퇴원',   'DISCHARGED', 1, TRUE, 1),
  ('PATIENT_STATUS', 'FOLLOW_UP',  '추적관찰', 'FOLLOW_UP', 2, TRUE, 1),
  ('PATIENT_STATUS', 'INACTIVE',   '비활성', 'INACTIVE',   3, TRUE, 1)
ON CONFLICT DO NOTHING;

-- Department
INSERT INTO code_groups (group_key, group_name, use_yn, tenant_id)
VALUES ('DEPARTMENT', '진료과', TRUE, 1)
ON CONFLICT DO NOTHING;

INSERT INTO codes (group_key, code, name, value, sort_order, use_yn, tenant_id) VALUES
  ('DEPARTMENT', 'IM',    '내과',     'IM',    0, TRUE, 1),
  ('DEPARTMENT', 'GS',    '외과',     'GS',    1, TRUE, 1),
  ('DEPARTMENT', 'NR',    '신경과',   'NR',    2, TRUE, 1),
  ('DEPARTMENT', 'CD',    '심장내과', 'CD',    3, TRUE, 1),
  ('DEPARTMENT', 'OG',    '산부인과', 'OG',    4, TRUE, 1),
  ('DEPARTMENT', 'PD',    '소아과',   'PD',    5, TRUE, 1),
  ('DEPARTMENT', 'OS',    '정형외과', 'OS',    6, TRUE, 1),
  ('DEPARTMENT', 'DR',    '피부과',   'DR',    7, TRUE, 1)
ON CONFLICT DO NOTHING;

-- Blood Type
INSERT INTO code_groups (group_key, group_name, use_yn, tenant_id)
VALUES ('BLOOD_TYPE', '혈액형', TRUE, 1)
ON CONFLICT DO NOTHING;

INSERT INTO codes (group_key, code, name, value, sort_order, use_yn, tenant_id) VALUES
  ('BLOOD_TYPE', 'A_POS',  'A+',  'A+',  0, TRUE, 1),
  ('BLOOD_TYPE', 'A_NEG',  'A-',  'A-',  1, TRUE, 1),
  ('BLOOD_TYPE', 'B_POS',  'B+',  'B+',  2, TRUE, 1),
  ('BLOOD_TYPE', 'B_NEG',  'B-',  'B-',  3, TRUE, 1),
  ('BLOOD_TYPE', 'O_POS',  'O+',  'O+',  4, TRUE, 1),
  ('BLOOD_TYPE', 'O_NEG',  'O-',  'O-',  5, TRUE, 1),
  ('BLOOD_TYPE', 'AB_POS', 'AB+', 'AB+', 6, TRUE, 1),
  ('BLOOD_TYPE', 'AB_NEG', 'AB-', 'AB-', 7, TRUE, 1)
ON CONFLICT DO NOTHING;

-- Gender
INSERT INTO code_groups (group_key, group_name, use_yn, tenant_id)
VALUES ('GENDER', '성별', TRUE, 1)
ON CONFLICT DO NOTHING;

INSERT INTO codes (group_key, code, name, value, sort_order, use_yn, tenant_id) VALUES
  ('GENDER', 'M', '남성', 'M', 0, TRUE, 1),
  ('GENDER', 'F', '여성', 'F', 1, TRUE, 1)
ON CONFLICT DO NOTHING;

-- Trial Phase
INSERT INTO code_groups (group_key, group_name, use_yn, tenant_id)
VALUES ('TRIAL_PHASE', '임상시험 단계', TRUE, 1)
ON CONFLICT DO NOTHING;

INSERT INTO codes (group_key, code, name, value, sort_order, use_yn, tenant_id) VALUES
  ('TRIAL_PHASE', 'PHASE_1', 'Phase I',   'PHASE_1', 0, TRUE, 1),
  ('TRIAL_PHASE', 'PHASE_2', 'Phase II',  'PHASE_2', 1, TRUE, 1),
  ('TRIAL_PHASE', 'PHASE_3', 'Phase III', 'PHASE_3', 2, TRUE, 1),
  ('TRIAL_PHASE', 'PHASE_4', 'Phase IV',  'PHASE_4', 3, TRUE, 1)
ON CONFLICT DO NOTHING;

-- Trial Status
INSERT INTO code_groups (group_key, group_name, use_yn, tenant_id)
VALUES ('TRIAL_STATUS', '임상시험 상태', TRUE, 1)
ON CONFLICT DO NOTHING;

INSERT INTO codes (group_key, code, name, value, sort_order, use_yn, tenant_id) VALUES
  ('TRIAL_STATUS', 'PLANNED',    '계획',   'PLANNED',    0, TRUE, 1),
  ('TRIAL_STATUS', 'RECRUITING', '모집중', 'RECRUITING', 1, TRUE, 1),
  ('TRIAL_STATUS', 'ACTIVE',     '진행중', 'ACTIVE',     2, TRUE, 1),
  ('TRIAL_STATUS', 'COMPLETED',  '완료',   'COMPLETED',  3, TRUE, 1),
  ('TRIAL_STATUS', 'SUSPENDED',  '중단',   'SUSPENDED',  4, TRUE, 1)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 2. Sample menus for default tenant (tenant_id=1)
--    Between Dashboard (sort_order=0) and Boards (sort_order=10)
-- ============================================================

-- Samples group (sort_order=5, between Dashboard and Boards)
INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES (NULL, 'Medical', NULL, 'heart-pulse', 5, TRUE, 'GROUP', 1);

-- Get the ID of the group we just inserted
INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES (
  (SELECT menu_id FROM menus WHERE name='Medical' AND tenant_id=1 AND menu_type='GROUP' LIMIT 1),
  'Patients', '/sample/patients', 'users', 0, TRUE, 'MENU', 1
);

INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES (
  (SELECT menu_id FROM menus WHERE name='Medical' AND tenant_id=1 AND menu_type='GROUP' LIMIT 1),
  'Clinical Trials', '/sample/trials', 'flask-conical', 10, TRUE, 'MENU', 1
);

-- Grant ADMIN and USER roles access to the new menus
INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, r.role_key
FROM menus m
CROSS JOIN (VALUES ('ADMIN'), ('USER')) AS r(role_key)
WHERE m.tenant_id = 1
  AND m.name IN ('Medical', 'Patients', 'Clinical Trials')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 3. System tenant menus (tenant_id=0, for SUPER_ADMIN)
-- ============================================================

INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES (NULL, 'Medical', NULL, 'heart-pulse', 5, TRUE, 'GROUP', 0);

INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES (
  (SELECT menu_id FROM menus WHERE name='Medical' AND tenant_id=0 AND menu_type='GROUP' LIMIT 1),
  'Patients', '/sample/patients', 'users', 0, TRUE, 'MENU', 0
);

INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES (
  (SELECT menu_id FROM menus WHERE name='Medical' AND tenant_id=0 AND menu_type='GROUP' LIMIT 1),
  'Clinical Trials', '/sample/trials', 'flask-conical', 10, TRUE, 'MENU', 0
);

INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, 'SUPER_ADMIN'
FROM menus m
WHERE m.tenant_id = 0
  AND m.name IN ('Medical', 'Patients', 'Clinical Trials')
ON CONFLICT DO NOTHING;

-- Also insert common codes for system tenant (tenant_id=0)
INSERT INTO code_groups (group_key, group_name, use_yn, tenant_id)
VALUES
  ('PATIENT_STATUS', '환자 상태', TRUE, 0),
  ('DEPARTMENT', '진료과', TRUE, 0),
  ('BLOOD_TYPE', '혈액형', TRUE, 0),
  ('GENDER', '성별', TRUE, 0),
  ('TRIAL_PHASE', '임상시험 단계', TRUE, 0),
  ('TRIAL_STATUS', '임상시험 상태', TRUE, 0)
ON CONFLICT DO NOTHING;

INSERT INTO codes (group_key, code, name, value, sort_order, use_yn, tenant_id)
SELECT c.group_key, c.code, c.name, c.value, c.sort_order, c.use_yn, 0
FROM codes c WHERE c.tenant_id = 1
  AND c.group_key IN ('PATIENT_STATUS','DEPARTMENT','BLOOD_TYPE','GENDER','TRIAL_PHASE','TRIAL_STATUS')
ON CONFLICT DO NOTHING;
