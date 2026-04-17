-- ============================================================
-- V16: 알림 채널 선호도 (Notification Preferences)
--   - 사용자별 알림 채널 동의/활성화 관리
--   - 채널: EMAIL, SMS, KAKAO, PUSH (확장 가능)
-- ============================================================

CREATE TABLE notification_preferences (
    pref_id         BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    channel         VARCHAR(20)  NOT NULL,  -- EMAIL, SMS, KAKAO, PUSH
    enabled         BOOLEAN      NOT NULL DEFAULT FALSE,
    consented       BOOLEAN      NOT NULL DEFAULT FALSE,
    consented_at    TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, user_id, channel)
);

CREATE INDEX idx_np_tenant_user ON notification_preferences (tenant_id, user_id);

COMMENT ON TABLE  notification_preferences           IS '사용자별 알림 채널 선호도';
COMMENT ON COLUMN notification_preferences.channel   IS '알림 채널: EMAIL, SMS, KAKAO, PUSH';
COMMENT ON COLUMN notification_preferences.enabled   IS '채널 활성화 여부 (사용자가 토글)';
COMMENT ON COLUMN notification_preferences.consented IS '수신 동의 여부 (법적 동의)';

-- 기존 사용자에 대해 EMAIL 기본 레코드 생성 (tenant_id=1)
INSERT INTO notification_preferences (tenant_id, user_id, channel, enabled, consented)
SELECT 1, u.user_id, 'EMAIL', TRUE, FALSE
  FROM users u
 WHERE u.tenant_id = 1
ON CONFLICT DO NOTHING;

-- 공통코드 등록
INSERT INTO code_groups (group_key, group_name, use_yn, tenant_id) VALUES
    ('NOTIFICATION_CHANNEL', '알림 채널', TRUE, 1)
ON CONFLICT DO NOTHING;

INSERT INTO codes (group_key, code, name, value, sort_order, use_yn, tenant_id) VALUES
    ('NOTIFICATION_CHANNEL', 'EMAIL',  '이메일',       'EMAIL',  1, TRUE, 1),
    ('NOTIFICATION_CHANNEL', 'SMS',    'SMS',          'SMS',    2, TRUE, 1),
    ('NOTIFICATION_CHANNEL', 'KAKAO',  '카카오톡',     'KAKAO',  3, TRUE, 1),
    ('NOTIFICATION_CHANNEL', 'PUSH',   '앱 푸시',      'PUSH',   4, TRUE, 1)
ON CONFLICT DO NOTHING;
