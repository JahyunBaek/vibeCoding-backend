-- ============================================================
-- V20: 공통 워크플로우 시스템 (Phase 1)
--   - 시스템 템플릿 + 테넌트별 Copy-on-Customize
--   - 결재(approval) / 알림 연동을 post-function 으로 처리
-- ============================================================

-- ── 1. 워크플로우 상태 (글로벌 풀, 코드 안정성 보장) ──────
CREATE TABLE workflow_states (
  state_code   VARCHAR(50) PRIMARY KEY,
  state_name   VARCHAR(100) NOT NULL,
  state_type   VARCHAR(20)  NOT NULL DEFAULT 'INTERMEDIATE',  -- INITIAL | INTERMEDIATE | FINAL
  color        VARCHAR(20),                                   -- 배지 색상 힌트
  description  VARCHAR(300),
  sort_order   INT NOT NULL DEFAULT 0,
  created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE  workflow_states IS '워크플로우 상태 글로벌 풀 — 코드는 인스턴스 전역에서 동일';
COMMENT ON COLUMN workflow_states.state_type IS 'INITIAL: 시작상태 | INTERMEDIATE: 중간 | FINAL: 종료';

-- ── 2. 워크플로우 정의 (시스템 템플릿 + 테넌트 사본) ──────
CREATE TABLE workflow_definitions (
  workflow_id           BIGSERIAL PRIMARY KEY,
  tenant_id             BIGINT,                              -- NULL = 시스템 템플릿
  workflow_code         VARCHAR(50) NOT NULL,                -- 도메인 코드 (e.g. ITSM_INCIDENT)
  workflow_name         VARCHAR(200) NOT NULL,
  description           VARCHAR(500),
  entity_type           VARCHAR(80) NOT NULL,                -- 적용 엔티티 (e.g. Incident, Requirement)
  initial_state_code    VARCHAR(50) NOT NULL REFERENCES workflow_states(state_code),
  active_yn             BOOLEAN NOT NULL DEFAULT TRUE,
  is_template           BOOLEAN NOT NULL DEFAULT FALSE,      -- 시스템 템플릿 여부
  parent_workflow_id    BIGINT REFERENCES workflow_definitions(workflow_id),  -- 어떤 템플릿에서 복사됐는지
  template_version      INT,                                 -- 복사 시점의 템플릿 버전
  created_by            BIGINT,
  created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at            TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX uq_workflow_definitions_code ON workflow_definitions (COALESCE(tenant_id, 0), workflow_code);
CREATE INDEX idx_workflow_definitions_tenant ON workflow_definitions (tenant_id);
CREATE INDEX idx_workflow_definitions_entity ON workflow_definitions (tenant_id, entity_type);

COMMENT ON COLUMN workflow_definitions.tenant_id IS 'NULL = 시스템 템플릿 (SUPER_ADMIN 관리)';
COMMENT ON COLUMN workflow_definitions.workflow_code IS '코드는 시스템 전역에서 안정. 테넌트가 변경 불가';
COMMENT ON COLUMN workflow_definitions.entity_type IS '도메인 코드가 참조 (e.g. IncidentService → entity_type=Incident)';

-- ── 3. 전이 (Transition) ───────────────────────────────
CREATE TABLE workflow_transitions (
  transition_id    BIGSERIAL PRIMARY KEY,
  workflow_id      BIGINT NOT NULL REFERENCES workflow_definitions(workflow_id) ON DELETE CASCADE,
  action_code      VARCHAR(50) NOT NULL,                              -- 코드 안정 (SUBMIT, APPROVE, ...)
  action_name      VARCHAR(100) NOT NULL,
  from_state_code  VARCHAR(50) NOT NULL REFERENCES workflow_states(state_code),
  to_state_code    VARCHAR(50) NOT NULL REFERENCES workflow_states(state_code),
  button_color     VARCHAR(20) DEFAULT 'default',                     -- default | primary | success | warning | danger
  button_icon      VARCHAR(50),                                       -- lucide icon name (optional)
  comment_required BOOLEAN NOT NULL DEFAULT FALSE,                    -- 의견 입력 필수 여부
  auto_skip        BOOLEAN NOT NULL DEFAULT FALSE,                    -- 도착 시 즉시 다음으로 자동 전이
  sort_order       INT NOT NULL DEFAULT 0,
  active_yn        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_workflow_trans_workflow ON workflow_transitions (workflow_id);
CREATE INDEX idx_workflow_trans_from ON workflow_transitions (workflow_id, from_state_code);
CREATE UNIQUE INDEX uq_workflow_trans_code
  ON workflow_transitions (workflow_id, action_code, from_state_code);

-- ── 4. 전이 조건 (Conditions) — 누가 이 전이를 할 수 있는가 ──
CREATE TABLE workflow_transition_conditions (
  condition_id     BIGSERIAL PRIMARY KEY,
  transition_id    BIGINT NOT NULL REFERENCES workflow_transitions(transition_id) ON DELETE CASCADE,
  condition_type   VARCHAR(30) NOT NULL,
    -- ROLE | DEPT | USER | IS_REQUESTER | IS_ASSIGNEE | ANY
  role_key         VARCHAR(50),    -- condition_type=ROLE
  dept_id          BIGINT,         -- condition_type=DEPT
  user_id          BIGINT,         -- condition_type=USER
  sort_order       INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_workflow_cond_transition ON workflow_transition_conditions (transition_id);

COMMENT ON COLUMN workflow_transition_conditions.condition_type IS
  'ROLE/DEPT/USER: 명시 매칭 | IS_REQUESTER: 인스턴스 요청자 본인 | IS_ASSIGNEE: 인스턴스 담당자 | ANY: 모든 인증 사용자';

-- ── 5. 전이 후 액션 (Post Functions) — 결재/알림/할당 ──────
CREATE TABLE workflow_transition_post_actions (
  post_action_id   BIGSERIAL PRIMARY KEY,
  transition_id    BIGINT NOT NULL REFERENCES workflow_transitions(transition_id) ON DELETE CASCADE,
  action_type      VARCHAR(30) NOT NULL,
    -- REQUIRE_APPROVAL | NOTIFY | ASSIGN | UPDATE_FIELD
  approval_code    VARCHAR(80),    -- REQUIRE_APPROVAL: 결재 정책 코드
  notify_target    VARCHAR(50),    -- NOTIFY: REQUESTER | ASSIGNEE | ROLE:ADMIN | USER:123
  notify_template  VARCHAR(100),   -- 알림 템플릿 키
  field_path       VARCHAR(100),   -- UPDATE_FIELD: 변경할 필드
  field_value      VARCHAR(500),   -- 값
  sort_order       INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_workflow_post_transition ON workflow_transition_post_actions (transition_id);

COMMENT ON COLUMN workflow_transition_post_actions.action_type IS
  'REQUIRE_APPROVAL: 결재 시작 후 완료 시 자동 전이 | NOTIFY: 알림 발송 | ASSIGN: 담당자 변경 | UPDATE_FIELD: 필드 업데이트';

-- ── 6. 워크플로우 인스턴스 (실제 비즈니스 엔티티의 진행 상태) ──
CREATE TABLE workflow_instances (
  instance_id            BIGSERIAL PRIMARY KEY,
  tenant_id              BIGINT NOT NULL,
  workflow_id            BIGINT NOT NULL REFERENCES workflow_definitions(workflow_id),
  entity_type            VARCHAR(80) NOT NULL,
  entity_id              BIGINT NOT NULL,
  current_state_code     VARCHAR(50) NOT NULL REFERENCES workflow_states(state_code),
  status                 VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | PENDING_APPROVAL | COMPLETED | CANCELED
  requester_user_id      BIGINT,
  assignee_user_id       BIGINT,
  approval_document_id   BIGINT,                                   -- 결재 진행 중일 때
  pending_transition_id  BIGINT REFERENCES workflow_transitions(transition_id),  -- 결재 완료 후 적용할 전이
  created_at             TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at             TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX uq_workflow_instance_entity
  ON workflow_instances (tenant_id, entity_type, entity_id);
CREATE INDEX idx_workflow_instance_state
  ON workflow_instances (tenant_id, entity_type, current_state_code);
CREATE INDEX idx_workflow_instance_assignee
  ON workflow_instances (tenant_id, assignee_user_id, status);
CREATE INDEX idx_workflow_instance_approval
  ON workflow_instances (approval_document_id) WHERE approval_document_id IS NOT NULL;

COMMENT ON COLUMN workflow_instances.status IS
  'ACTIVE: 진행 중 | PENDING_APPROVAL: 결재 대기 | COMPLETED: 종료 상태 도달 | CANCELED: 강제 종료';

-- ── 7. 상태 변경 이력 ─────────────────────────────────────
CREATE TABLE workflow_history (
  history_id        BIGSERIAL PRIMARY KEY,
  instance_id       BIGINT NOT NULL REFERENCES workflow_instances(instance_id) ON DELETE CASCADE,
  transition_id     BIGINT REFERENCES workflow_transitions(transition_id),
  action_code       VARCHAR(50),
  from_state_code   VARCHAR(50),
  to_state_code     VARCHAR(50),
  actor_user_id     BIGINT,
  comment           VARCHAR(1000),
  meta              JSONB,                          -- 자유 메타 (담당자 변경, 결재문서ID 등)
  created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_workflow_history_instance ON workflow_history (instance_id, created_at DESC);

-- ============================================================
-- 시드 1: 글로벌 상태 풀
-- ============================================================
INSERT INTO workflow_states (state_code, state_name, state_type, color, sort_order) VALUES
  ('NEW',          '신규',     'INITIAL',      'blue',    1),
  ('ASSIGNED',     '할당됨',   'INTERMEDIATE', 'cyan',    2),
  ('IN_PROGRESS',  '진행 중',  'INTERMEDIATE', 'amber',   3),
  ('PENDING',      '대기',     'INTERMEDIATE', 'gray',    4),
  ('REVIEW',       '검토 중',  'INTERMEDIATE', 'purple',  5),
  ('APPROVED',     '승인됨',   'INTERMEDIATE', 'emerald', 6),
  ('REJECTED',     '반려됨',   'FINAL',        'red',     7),
  ('RESOLVED',     '해결됨',   'INTERMEDIATE', 'green',   8),
  ('CLOSED',       '종료',     'FINAL',        'slate',   9),
  ('CANCELED',     '취소됨',   'FINAL',        'gray',   10),
  ('REOPENED',     '재오픈',   'INTERMEDIATE', 'orange', 11),
  ('DRAFT',        '초안',     'INITIAL',      'gray',   12),
  ('SUBMITTED',    '제출됨',   'INTERMEDIATE', 'blue',   13)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 시드 2: 시스템 템플릿 — ITSM_INCIDENT (가장 일반적 예시)
-- 흐름: NEW → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED (REOPEN 가능)
-- ============================================================
INSERT INTO workflow_definitions
  (tenant_id, workflow_code, workflow_name, description, entity_type, initial_state_code,
   is_template, template_version, active_yn)
VALUES
  (NULL, 'ITSM_INCIDENT', 'ITSM 인시던트',
   'IT 서비스 인시던트 처리 표준 워크플로우 (NEW → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED)',
   'Incident', 'NEW', TRUE, 1, TRUE);

-- transitions
WITH wf AS (SELECT workflow_id FROM workflow_definitions WHERE tenant_id IS NULL AND workflow_code = 'ITSM_INCIDENT')
INSERT INTO workflow_transitions
  (workflow_id, action_code, action_name, from_state_code, to_state_code, button_color, sort_order)
SELECT wf.workflow_id, t.* FROM wf, (VALUES
  ('ASSIGN',  '담당자 지정', 'NEW',         'ASSIGNED',    'primary', 1),
  ('START',   '처리 시작',   'ASSIGNED',    'IN_PROGRESS', 'primary', 2),
  ('RESOLVE', '해결',       'IN_PROGRESS', 'RESOLVED',    'success', 3),
  ('CLOSE',   '종료',       'RESOLVED',    'CLOSED',      'default', 4),
  ('REOPEN',  '재오픈',     'RESOLVED',    'ASSIGNED',    'warning', 5),
  ('CANCEL',  '취소',       'NEW',         'CANCELED',    'danger',  6)
) AS t(action_code, action_name, from_state_code, to_state_code, button_color, sort_order);

-- conditions: ASSIGN/START/RESOLVE은 ADMIN, CLOSE/REOPEN은 요청자, CANCEL은 요청자
WITH t AS (
  SELECT tr.transition_id, tr.action_code
    FROM workflow_transitions tr
    JOIN workflow_definitions wf ON wf.workflow_id = tr.workflow_id
   WHERE wf.tenant_id IS NULL AND wf.workflow_code = 'ITSM_INCIDENT'
)
INSERT INTO workflow_transition_conditions (transition_id, condition_type, role_key, sort_order)
SELECT transition_id, c.condition_type, c.role_key, 0
FROM t, LATERAL (VALUES
  ('ASSIGN',  'ROLE',         'ADMIN'),
  ('START',   'IS_ASSIGNEE',  NULL),
  ('RESOLVE', 'IS_ASSIGNEE',  NULL),
  ('CLOSE',   'IS_REQUESTER', NULL),
  ('REOPEN',  'IS_REQUESTER', NULL),
  ('CANCEL',  'IS_REQUESTER', NULL)
) AS c(action_code, condition_type, role_key)
WHERE t.action_code = c.action_code;

-- post-actions: 알림만 (결재는 시드에서 제외 — 도메인 도입 시 추가)
WITH t AS (
  SELECT tr.transition_id, tr.action_code
    FROM workflow_transitions tr
    JOIN workflow_definitions wf ON wf.workflow_id = tr.workflow_id
   WHERE wf.tenant_id IS NULL AND wf.workflow_code = 'ITSM_INCIDENT'
)
INSERT INTO workflow_transition_post_actions
  (transition_id, action_type, notify_target, notify_template, sort_order)
SELECT transition_id, 'NOTIFY', n.notify_target, n.notify_template, 0
FROM t, LATERAL (VALUES
  ('ASSIGN',  'ASSIGNEE',  'workflow.assigned'),
  ('RESOLVE', 'REQUESTER', 'workflow.resolved'),
  ('CLOSE',   'ASSIGNEE',  'workflow.closed'),
  ('REOPEN',  'ASSIGNEE',  'workflow.reopened')
) AS n(action_code, notify_target, notify_template)
WHERE t.action_code = n.action_code;
