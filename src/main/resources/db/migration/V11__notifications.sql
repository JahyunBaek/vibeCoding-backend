CREATE TABLE IF NOT EXISTS notifications (
  notification_id BIGSERIAL PRIMARY KEY,
  tenant_id       BIGINT REFERENCES tenants(tenant_id),
  user_id         BIGINT NOT NULL REFERENCES users(user_id),
  type            VARCHAR(50) NOT NULL,   -- COMMENT, NOTICE, SYSTEM, PASSWORD_RESET, ROLE_CHANGE
  title           VARCHAR(200) NOT NULL,
  message         TEXT,
  link            VARCHAR(500),
  read_yn         BOOLEAN DEFAULT FALSE,
  created_at      TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id, read_yn, created_at DESC);
