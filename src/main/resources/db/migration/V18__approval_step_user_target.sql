-- ============================================================
-- V18: 결재 단계에 사용자(USER) 지정 타입 추가
-- ============================================================

-- 1. approval_line_template_step: target_user_id 컬럼 추가
ALTER TABLE approval_line_template_step
  ADD COLUMN IF NOT EXISTS target_user_id BIGINT NULL REFERENCES users(user_id);

-- 2. approval_document_step: target_user_id 컬럼 추가
ALTER TABLE approval_document_step
  ADD COLUMN IF NOT EXISTS target_user_id BIGINT NULL REFERENCES users(user_id);

-- 3. 컬럼 코멘트 갱신 (기존 type: REQUEST | SUPERVISING | CUSTOM | USER)
COMMENT ON COLUMN approval_line_template_step.target_department_type
  IS 'REQUEST | SUPERVISING | CUSTOM | USER (USER 타입은 target_user_id 필수)';
COMMENT ON COLUMN approval_document_step.target_department_type
  IS 'REQUEST | SUPERVISING | CUSTOM | USER (USER 타입은 target_user_id 필수)';

COMMENT ON COLUMN approval_line_template_step.target_user_id
  IS '특정 사용자 지정 (target_department_type=USER일 때 사용)';
COMMENT ON COLUMN approval_document_step.target_user_id
  IS '특정 사용자 지정 (target_department_type=USER일 때 사용)';
