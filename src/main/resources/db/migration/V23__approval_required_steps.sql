SET search_path TO testdb;

-- ============================================================
-- 결재 정책별 "필수 단계" — 사용자 결재선 뒤에 강제로 붙는 단계
--
-- 사용 예:
--   - 정책에서 "주관부서 승인" 단계를 필수로 등록
--   - 사용자 결재선(팀장→부서장) + 정책 필수단계(주관부서장)가 자동 합쳐짐
--   - 사용자는 정책 필수단계를 수정/삭제할 수 없으며, 그 앞 단계만 자유롭게 추가
--
-- target_department_type:
--   REQUEST     — 신청자 소속 부서 (상신 시 동적 치환)
--   SUPERVISING — 주관부서 (정책 또는 요청에서 지정한 부서)
--   CUSTOM      — 특정 부서 (target_department_id 사용)
--   USER        — 특정 사용자 (target_user_id 사용)
-- ============================================================

CREATE TABLE IF NOT EXISTS approval_definition_required_step (
  required_step_id        BIGSERIAL PRIMARY KEY,
  definition_id           BIGINT       NOT NULL REFERENCES approval_definition(definition_id) ON DELETE CASCADE,
  step_order              INT          NOT NULL,
  step_name               VARCHAR(100) NOT NULL,
  approval_type           VARCHAR(30)  NOT NULL DEFAULT 'APPROVE',
  target_department_type  VARCHAR(30)  NOT NULL,
  target_department_id    BIGINT       NULL REFERENCES orgs(org_id),
  target_role_key         VARCHAR(50)  NULL REFERENCES roles(role_key),
  target_user_id          BIGINT       NULL REFERENCES users(user_id),
  group_approval_yn       BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at              TIMESTAMP    NOT NULL DEFAULT NOW(),
  updated_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_approval_def_req_step_def
  ON approval_definition_required_step(definition_id, step_order);
