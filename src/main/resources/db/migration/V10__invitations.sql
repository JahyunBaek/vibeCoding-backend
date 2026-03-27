CREATE TABLE IF NOT EXISTS invitations (
  invitation_id BIGSERIAL PRIMARY KEY,
  tenant_id     BIGINT NOT NULL REFERENCES tenants(tenant_id),
  email         VARCHAR(255) NOT NULL,
  role_key      VARCHAR(50) NOT NULL DEFAULT 'USER',
  token         VARCHAR(100) NOT NULL UNIQUE,
  status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, ACCEPTED, EXPIRED
  invited_by    BIGINT NOT NULL REFERENCES users(user_id),
  created_at    TIMESTAMP DEFAULT NOW(),
  expires_at    TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_invitations_token ON invitations(token);
CREATE INDEX IF NOT EXISTS idx_invitations_tenant ON invitations(tenant_id);
