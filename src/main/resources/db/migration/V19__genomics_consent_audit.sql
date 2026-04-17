-- ============================================================
-- V19: Phase 6 — 동의서 관리, 유전체 접근 감사 로그
-- ============================================================

-- ── 1. 유전자 검사 동의서 ──────────────────────────────────
CREATE TABLE genomic_consents (
    consent_id      BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    patient_id      BIGINT       NOT NULL,
    sample_id       BIGINT       REFERENCES genomic_samples(sample_id),
    consent_type    VARCHAR(30)  NOT NULL DEFAULT 'GENETIC_TEST',
        -- GENETIC_TEST, RESEARCH_USE, DATA_SHARING, BIOBANK
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
        -- PENDING, SIGNED, REVOKED, EXPIRED
    signed_at       TIMESTAMP,
    revoked_at      TIMESTAMP,
    expires_at      DATE,
    signed_by_name  VARCHAR(100),
    witness_name    VARCHAR(100),
    note            TEXT,
    created_by      BIGINT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gc_tenant  ON genomic_consents (tenant_id);
CREATE INDEX idx_gc_patient ON genomic_consents (tenant_id, patient_id);
CREATE INDEX idx_gc_sample  ON genomic_consents (sample_id);

COMMENT ON TABLE  genomic_consents              IS '유전자 검사 동의서';
COMMENT ON COLUMN genomic_consents.consent_type IS 'GENETIC_TEST, RESEARCH_USE, DATA_SHARING, BIOBANK';
COMMENT ON COLUMN genomic_consents.status       IS 'PENDING, SIGNED, REVOKED, EXPIRED';

-- ── 2. 유전체 데이터 접근 감사 로그 ────────────────────────
CREATE TABLE genomic_audit_logs (
    audit_id        BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    action          VARCHAR(30)  NOT NULL,
        -- VIEW_SAMPLE, VIEW_VARIANT, VIEW_REPORT, EXPORT_DATA, UPLOAD_VCF, GENERATE_REPORT, AI_INTERPRET
    resource_type   VARCHAR(30)  NOT NULL,
        -- SAMPLE, VARIANT, REPORT, PGX, EXPORT
    resource_id     BIGINT,
    detail          TEXT,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gal_tenant  ON genomic_audit_logs (tenant_id);
CREATE INDEX idx_gal_user    ON genomic_audit_logs (tenant_id, user_id);
CREATE INDEX idx_gal_created ON genomic_audit_logs (created_at);

COMMENT ON TABLE genomic_audit_logs IS '유전체 데이터 접근 감사 로그 (HIPAA/개인정보보호법 대응)';

-- ── 3. 공통코드 ─────────────────────────────────────────────
INSERT INTO code_groups (group_key, group_name, use_yn, tenant_id) VALUES
    ('CONSENT_TYPE',   '동의서 유형',   TRUE, 1),
    ('CONSENT_STATUS', '동의서 상태',   TRUE, 1)
ON CONFLICT DO NOTHING;

INSERT INTO codes (group_key, code, name, value, sort_order, use_yn, tenant_id) VALUES
    ('CONSENT_TYPE', 'GENETIC_TEST',  '유전자 검사 동의',   'GENETIC_TEST',  1, TRUE, 1),
    ('CONSENT_TYPE', 'RESEARCH_USE',  '연구 목적 사용 동의', 'RESEARCH_USE',  2, TRUE, 1),
    ('CONSENT_TYPE', 'DATA_SHARING',  '데이터 공유 동의',   'DATA_SHARING',  3, TRUE, 1),
    ('CONSENT_TYPE', 'BIOBANK',       '바이오뱅크 동의',    'BIOBANK',       4, TRUE, 1),
    ('CONSENT_STATUS', 'PENDING',  '대기',   'PENDING',  1, TRUE, 1),
    ('CONSENT_STATUS', 'SIGNED',   '서명완료', 'SIGNED',   2, TRUE, 1),
    ('CONSENT_STATUS', 'REVOKED',  '철회',   'REVOKED',  3, TRUE, 1),
    ('CONSENT_STATUS', 'EXPIRED',  '만료',   'EXPIRED',  4, TRUE, 1)
ON CONFLICT DO NOTHING;

-- ── 4. 메뉴 추가 ───────────────────────────────────────────
INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES
    ((SELECT menu_id FROM menus WHERE name='Genomics' AND tenant_id=1 LIMIT 1),
     'Consents', '/genomics/consents', 'file-signature', 7, TRUE, 'MENU', 1);

INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, r.role_key
FROM menus m
CROSS JOIN (VALUES ('ADMIN'), ('USER')) AS r(role_key)
WHERE m.tenant_id = 1 AND m.name = 'Consents' AND m.path = '/genomics/consents'
ON CONFLICT DO NOTHING;
