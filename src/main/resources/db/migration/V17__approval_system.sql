SET search_path TO testdb;

-- ============================================================
-- 공통 결재(Approval) 시스템 — MVP 1차
--
-- 구성:
-- 1. approval_definition        — 결재 기능 코드 마스터 (관리자 설정)
-- 2. approval_authority_rule    — 부서/역할 결재 권한 규칙 (그룹결재 대상 정의)
-- 3. approval_line_template     — 개인 결재선 양식 헤더
-- 4. approval_line_template_step — 개인 결재선 양식 단계
-- 5. approval_document          — 실제 결재 문서
-- 6. approval_document_step     — 실제 결재 단계
-- 7. approval_history           — 결재 이력 (감사/추적)
-- ============================================================

-- ----------------------------------------------------
-- 1. approval_definition: 결재 기능 코드 마스터
-- ----------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_definition (
  definition_id                  BIGSERIAL PRIMARY KEY,
  tenant_id                      BIGINT       NOT NULL REFERENCES tenants(tenant_id),
  approval_code                  VARCHAR(80)  NOT NULL,
  approval_name                  VARCHAR(200) NOT NULL,
  description                    TEXT,
  use_request_department         BOOLEAN      NOT NULL DEFAULT TRUE,
  use_supervising_department     BOOLEAN      NOT NULL DEFAULT FALSE,
  default_supervising_department_id BIGINT    NULL REFERENCES orgs(org_id),
  use_group_approval             BOOLEAN      NOT NULL DEFAULT TRUE,
  use_personal_line_template     BOOLEAN      NOT NULL DEFAULT TRUE,
  active_yn                      BOOLEAN      NOT NULL DEFAULT TRUE,
  sort_order                     INT          NOT NULL DEFAULT 0,
  remark                         TEXT,
  created_by                     BIGINT       NULL,
  created_at                     TIMESTAMP    NOT NULL DEFAULT NOW(),
  updated_by                     BIGINT       NULL,
  updated_at                     TIMESTAMP    NOT NULL DEFAULT NOW(),
  UNIQUE (tenant_id, approval_code)
);

CREATE INDEX IF NOT EXISTS idx_approval_def_tenant       ON approval_definition(tenant_id);
CREATE INDEX IF NOT EXISTS idx_approval_def_active       ON approval_definition(tenant_id, active_yn);

-- ----------------------------------------------------
-- 2. approval_authority_rule: 부서별 결재 권한자 정의
--    (그룹결재 시 결재 가능 사용자 판별용)
-- ----------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_authority_rule (
  rule_id              BIGSERIAL PRIMARY KEY,
  tenant_id            BIGINT      NOT NULL REFERENCES tenants(tenant_id),
  approval_code        VARCHAR(80) NOT NULL,
  target_department_id BIGINT      NULL REFERENCES orgs(org_id),
  target_role_key      VARCHAR(50) NULL REFERENCES roles(role_key),
  step_type            VARCHAR(30) NOT NULL,  -- REQUEST_DEPT | SUPERVISING_DEPT | CUSTOM
  active_yn            BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at           TIMESTAMP   NOT NULL DEFAULT NOW(),
  updated_at           TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_approval_authority_tenant_code
  ON approval_authority_rule(tenant_id, approval_code, active_yn);

-- ----------------------------------------------------
-- 3. approval_line_template: 개인 결재선 양식 헤더
-- ----------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_line_template (
  template_id    BIGSERIAL PRIMARY KEY,
  tenant_id      BIGINT       NOT NULL REFERENCES tenants(tenant_id),
  approval_code  VARCHAR(80)  NOT NULL,
  owner_user_id  BIGINT       NOT NULL REFERENCES users(user_id),
  template_name  VARCHAR(200) NOT NULL,
  default_yn     BOOLEAN      NOT NULL DEFAULT FALSE,
  active_yn      BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_approval_tpl_owner
  ON approval_line_template(tenant_id, owner_user_id, approval_code, active_yn);

-- 한 사용자 + 한 코드에 default는 최대 1개
CREATE UNIQUE INDEX IF NOT EXISTS uk_approval_tpl_default
  ON approval_line_template(tenant_id, owner_user_id, approval_code)
  WHERE default_yn = TRUE;

-- ----------------------------------------------------
-- 4. approval_line_template_step: 개인 결재선 양식 단계
-- ----------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_line_template_step (
  step_id                BIGSERIAL PRIMARY KEY,
  template_id            BIGINT      NOT NULL REFERENCES approval_line_template(template_id) ON DELETE CASCADE,
  step_order             INT         NOT NULL,
  step_name              VARCHAR(100) NOT NULL,
  approval_type          VARCHAR(30) NOT NULL DEFAULT 'APPROVE',  -- APPROVE | REVIEW | CONSENT (2차 확장)
  target_department_type VARCHAR(30) NOT NULL,                    -- REQUEST | SUPERVISING | CUSTOM
  target_department_id   BIGINT      NULL REFERENCES orgs(org_id),
  target_role_key        VARCHAR(50) NULL REFERENCES roles(role_key),
  group_approval_yn      BOOLEAN     NOT NULL DEFAULT TRUE,
  required_yn            BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_approval_tpl_step_template
  ON approval_line_template_step(template_id, step_order);

-- ----------------------------------------------------
-- 5. approval_document: 결재 문서
-- ----------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_document (
  document_id                 BIGSERIAL PRIMARY KEY,
  tenant_id                   BIGINT       NOT NULL REFERENCES tenants(tenant_id),
  document_no                 VARCHAR(50)  NOT NULL,    -- AP-{tenantId}-{YYYYMM}-{seq}
  approval_code               VARCHAR(80)  NOT NULL,
  business_type               VARCHAR(80)  NULL,        -- 업무 원문서 타입 (선택)
  business_id                 VARCHAR(100) NULL,        -- 업무 원문서 식별자
  title                       VARCHAR(300) NOT NULL,
  body                        TEXT,                     -- 결재 본문 (선택)
  requester_user_id           BIGINT       NOT NULL REFERENCES users(user_id),
  requester_department_id     BIGINT       NULL REFERENCES orgs(org_id),
  supervising_department_id   BIGINT       NULL REFERENCES orgs(org_id),
  status                      VARCHAR(20)  NOT NULL,    -- DRAFT | IN_PROGRESS | APPROVED | REJECTED | CANCELED | WITHDRAWN
  current_step_order          INT          NULL,
  requested_at                TIMESTAMP    NULL,
  completed_at                TIMESTAMP    NULL,
  withdrawn_at                TIMESTAMP    NULL,
  canceled_at                 TIMESTAMP    NULL,
  created_at                  TIMESTAMP    NOT NULL DEFAULT NOW(),
  updated_at                  TIMESTAMP    NOT NULL DEFAULT NOW(),
  UNIQUE (tenant_id, document_no)
);

CREATE INDEX IF NOT EXISTS idx_approval_doc_requester   ON approval_document(tenant_id, requester_user_id, status);
CREATE INDEX IF NOT EXISTS idx_approval_doc_status      ON approval_document(tenant_id, status, requested_at DESC);
CREATE INDEX IF NOT EXISTS idx_approval_doc_code        ON approval_document(tenant_id, approval_code);
CREATE INDEX IF NOT EXISTS idx_approval_doc_business    ON approval_document(tenant_id, business_type, business_id);

-- ----------------------------------------------------
-- 6. approval_document_step: 결재 문서 단계
-- ----------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_document_step (
  step_id                BIGSERIAL PRIMARY KEY,
  document_id            BIGINT       NOT NULL REFERENCES approval_document(document_id) ON DELETE CASCADE,
  step_order             INT          NOT NULL,
  step_name              VARCHAR(100) NOT NULL,
  approval_type          VARCHAR(30)  NOT NULL DEFAULT 'APPROVE',
  target_department_type VARCHAR(30)  NOT NULL,  -- REQUEST | SUPERVISING | CUSTOM
  target_department_id   BIGINT       NULL REFERENCES orgs(org_id),
  target_role_key        VARCHAR(50)  NULL REFERENCES roles(role_key),
  group_approval_yn      BOOLEAN      NOT NULL DEFAULT TRUE,
  status                 VARCHAR(20)  NOT NULL DEFAULT 'PENDING',  -- PENDING | APPROVED | REJECTED | SKIPPED
  acted_by_user_id       BIGINT       NULL REFERENCES users(user_id),
  acted_at               TIMESTAMP    NULL,
  comment                TEXT,
  created_at             TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_approval_doc_step_document
  ON approval_document_step(document_id, step_order);
CREATE INDEX IF NOT EXISTS idx_approval_doc_step_status
  ON approval_document_step(document_id, status);

-- ----------------------------------------------------
-- 7. approval_history: 이력 (감사/추적)
-- ----------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_history (
  history_id      BIGSERIAL PRIMARY KEY,
  tenant_id       BIGINT      NOT NULL REFERENCES tenants(tenant_id),
  document_id     BIGINT      NOT NULL REFERENCES approval_document(document_id) ON DELETE CASCADE,
  step_id         BIGINT      NULL REFERENCES approval_document_step(step_id),
  action_type     VARCHAR(30) NOT NULL,   -- REQUEST | APPROVE | REJECT | CANCEL | WITHDRAW
  action_by       BIGINT      NOT NULL REFERENCES users(user_id),
  action_comment  TEXT,
  action_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
  before_status   VARCHAR(20),
  after_status    VARCHAR(20)
);

CREATE INDEX IF NOT EXISTS idx_approval_history_document ON approval_history(document_id, action_at);

-- ============================================================
-- 화면/액션/역할 권한 등록 (글로벌)
-- ============================================================

-- 관리자 화면: 결재 정책 관리
INSERT INTO screens (screen_key, screen_name, use_yn)
VALUES ('ADMIN_APPROVAL_DEF', '결재 정책 관리', TRUE)
ON CONFLICT DO NOTHING;

-- 개인 화면: 내 결재선 양식 관리
INSERT INTO screens (screen_key, screen_name, use_yn)
VALUES ('MY_APPROVAL_LINE', '내 결재선 양식', TRUE)
ON CONFLICT DO NOTHING;

-- 결재 문서 (공통 현황)
INSERT INTO screens (screen_key, screen_name, use_yn)
VALUES ('APPROVAL_DOCUMENT', '결재 문서', TRUE)
ON CONFLICT DO NOTHING;

-- 액션
INSERT INTO screen_actions (screen_id, action_key, action_name, use_yn)
SELECT s.screen_id, a.action_key, a.action_name, TRUE
FROM screens s
CROSS JOIN (VALUES
  ('CREATE', '생성'),
  ('EDIT',   '수정'),
  ('DELETE', '삭제'),
  ('MANAGE', '관리')
) AS a(action_key, action_name)
WHERE s.screen_key = 'ADMIN_APPROVAL_DEF'
ON CONFLICT DO NOTHING;

INSERT INTO screen_actions (screen_id, action_key, action_name, use_yn)
SELECT s.screen_id, a.action_key, a.action_name, TRUE
FROM screens s
CROSS JOIN (VALUES
  ('CREATE', '생성'),
  ('EDIT',   '수정'),
  ('DELETE', '삭제')
) AS a(action_key, action_name)
WHERE s.screen_key = 'MY_APPROVAL_LINE'
ON CONFLICT DO NOTHING;

INSERT INTO screen_actions (screen_id, action_key, action_name, use_yn)
SELECT s.screen_id, a.action_key, a.action_name, TRUE
FROM screens s
CROSS JOIN (VALUES
  ('REQUEST',  '상신'),
  ('APPROVE',  '승인'),
  ('REJECT',   '반려'),
  ('WITHDRAW', '회수'),
  ('CANCEL',   '취소')
) AS a(action_key, action_name)
WHERE s.screen_key = 'APPROVAL_DOCUMENT'
ON CONFLICT DO NOTHING;

-- 역할 배정: 모든 테넌트
-- ADMIN: ADMIN_APPROVAL_DEF 전부 + MY_APPROVAL_LINE 전부 + APPROVAL_DOCUMENT 전부
INSERT INTO role_actions (tenant_id, role_key, action_id)
SELECT t.tenant_id, 'ADMIN', sa.action_id
FROM tenants t
CROSS JOIN screen_actions sa
JOIN screens s ON s.screen_id = sa.screen_id
WHERE s.screen_key IN ('ADMIN_APPROVAL_DEF', 'MY_APPROVAL_LINE', 'APPROVAL_DOCUMENT')
ON CONFLICT DO NOTHING;

-- USER: MY_APPROVAL_LINE + APPROVAL_DOCUMENT
INSERT INTO role_actions (tenant_id, role_key, action_id)
SELECT t.tenant_id, 'USER', sa.action_id
FROM tenants t
CROSS JOIN screen_actions sa
JOIN screens s ON s.screen_id = sa.screen_id
WHERE s.screen_key IN ('MY_APPROVAL_LINE', 'APPROVAL_DOCUMENT')
ON CONFLICT DO NOTHING;

-- SUPER_ADMIN (tenant_id = 0): 전부
INSERT INTO role_actions (tenant_id, role_key, action_id)
SELECT 0, 'SUPER_ADMIN', sa.action_id
FROM screen_actions sa
JOIN screens s ON s.screen_id = sa.screen_id
WHERE s.screen_key IN ('ADMIN_APPROVAL_DEF', 'MY_APPROVAL_LINE', 'APPROVAL_DOCUMENT')
ON CONFLICT DO NOTHING;

-- ============================================================
-- 메뉴 등록 (각 테넌트)
--   - 결재 그룹(Approval)
--     - 내 결재함 (/approval/my)
--     - 내 결재선 양식 (/approval/lines)
--   - 관리자(Admin) 하위
--     - 결재 정책 관리 (/admin/approval)
-- ============================================================

-- 모든 활성 테넌트에 대해 '결재(Approval)' 그룹 + 하위 메뉴 생성
INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
SELECT NULL, 'Approval', NULL, 'file-signature', 7, TRUE, 'GROUP', t.tenant_id
FROM tenants t
WHERE NOT EXISTS (
  SELECT 1 FROM menus m WHERE m.tenant_id = t.tenant_id AND m.name = 'Approval' AND m.menu_type = 'GROUP'
);

-- 내 결재함
INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
SELECT g.menu_id, 'My Approvals', '/approval/my', 'inbox', 0, TRUE, 'MENU', t.tenant_id
FROM tenants t
JOIN menus g ON g.tenant_id = t.tenant_id AND g.name = 'Approval' AND g.menu_type = 'GROUP'
WHERE NOT EXISTS (
  SELECT 1 FROM menus m WHERE m.tenant_id = t.tenant_id AND m.path = '/approval/my'
);

-- 내 결재선 양식
INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
SELECT g.menu_id, 'Approval Lines', '/approval/lines', 'list-tree', 10, TRUE, 'MENU', t.tenant_id
FROM tenants t
JOIN menus g ON g.tenant_id = t.tenant_id AND g.name = 'Approval' AND g.menu_type = 'GROUP'
WHERE NOT EXISTS (
  SELECT 1 FROM menus m WHERE m.tenant_id = t.tenant_id AND m.path = '/approval/lines'
);

-- 관리자: 결재 정책 관리 (admin 그룹 하위)
INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
SELECT a.menu_id, 'Approval Definitions', '/admin/approval', 'stamp', 95, TRUE, 'MENU', t.tenant_id
FROM tenants t
LEFT JOIN menus a ON a.tenant_id = t.tenant_id AND a.name = 'Admin' AND a.menu_type = 'GROUP'
WHERE a.menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM menus m WHERE m.tenant_id = t.tenant_id AND m.path = '/admin/approval'
  );

-- 메뉴-역할 매핑
INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, r.role_key
FROM menus m
CROSS JOIN (VALUES ('ADMIN'), ('USER')) AS r(role_key)
WHERE m.path IN ('/approval/my', '/approval/lines')
ON CONFLICT DO NOTHING;

INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, 'ADMIN'
FROM menus m
WHERE m.path = '/admin/approval'
ON CONFLICT DO NOTHING;

-- Approval GROUP 자체 노출: ADMIN + USER (+ tenant=0은 SUPER_ADMIN)
INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, r.role_key
FROM menus m
CROSS JOIN (VALUES ('ADMIN'), ('USER')) AS r(role_key)
WHERE m.name = 'Approval' AND m.menu_type = 'GROUP' AND m.tenant_id > 0
ON CONFLICT DO NOTHING;

INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, 'SUPER_ADMIN'
FROM menus m
WHERE m.tenant_id = 0
  AND (m.name = 'Approval' OR m.path IN ('/approval/my', '/approval/lines', '/admin/approval'))
ON CONFLICT DO NOTHING;
