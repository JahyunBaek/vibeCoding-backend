-- ============================================================
-- V17: Phase 3~4 — PGx 테이블, 보고서 테이블, 메뉴 추가
-- ============================================================

-- ── 1. 약물유전체(PGx) 매핑 테이블 ─────────────────────────
CREATE TABLE pgx_mappings (
    pgx_id          BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    gene_symbol     VARCHAR(30)  NOT NULL,           -- e.g. CYP2D6, CYP2C19
    variant_name    VARCHAR(100) NOT NULL,           -- e.g. *2, *3, rs1234
    drug_name       VARCHAR(100) NOT NULL,           -- e.g. Tamoxifen, Clopidogrel
    effect          VARCHAR(30)  NOT NULL,           -- POOR_METABOLIZER, INTERMEDIATE, NORMAL, RAPID
    recommendation  TEXT,                            -- 처방 권고
    evidence_level  VARCHAR(10),                     -- 1A, 1B, 2A, 2B, 3, 4
    source          VARCHAR(50),                     -- CPIC, DPWG, FDA
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, gene_symbol, variant_name, drug_name)
);

CREATE INDEX idx_pgx_tenant ON pgx_mappings (tenant_id);
CREATE INDEX idx_pgx_gene   ON pgx_mappings (gene_symbol);

COMMENT ON TABLE  pgx_mappings         IS '약물유전체(PGx) 유전자-약물 매핑';
COMMENT ON COLUMN pgx_mappings.effect  IS '대사 표현형: POOR_METABOLIZER, INTERMEDIATE, NORMAL, RAPID';

-- ── 2. 보고서 테이블 ────────────────────────────────────────
CREATE TABLE genomic_reports (
    report_id       BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    sample_id       BIGINT       NOT NULL REFERENCES genomic_samples(sample_id),
    title           VARCHAR(200) NOT NULL,
    summary         TEXT,                            -- AI 생성 요약
    variant_count   INT          NOT NULL DEFAULT 0,
    pathogenic_count INT         NOT NULL DEFAULT 0,
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',  -- DRAFT, FINAL
    created_by      BIGINT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gr_tenant ON genomic_reports (tenant_id);
CREATE INDEX idx_gr_sample ON genomic_reports (sample_id);

COMMENT ON TABLE genomic_reports IS '유전체 분석 보고서';

-- ── 3. PGx 샘플 데이터 ─────────────────────────────────────
INSERT INTO pgx_mappings (tenant_id, gene_symbol, variant_name, drug_name, effect, recommendation, evidence_level, source) VALUES
    (1, 'CYP2D6',  '*4',  'Tamoxifen',    'POOR_METABOLIZER', 'Consider alternative endocrine therapy (e.g., aromatase inhibitor). Avoid tamoxifen in poor metabolizers.',          '1A', 'CPIC'),
    (1, 'CYP2D6',  '*4',  'Codeine',      'POOR_METABOLIZER', 'Avoid codeine. Use alternative analgesics not metabolized by CYP2D6.',                                               '1A', 'CPIC'),
    (1, 'CYP2D6',  '*10', 'Tamoxifen',    'INTERMEDIATE',     'Consider higher dose or alternative therapy. Monitor response.',                                                      '1B', 'CPIC'),
    (1, 'CYP2C19', '*2',  'Clopidogrel',  'POOR_METABOLIZER', 'Use alternative antiplatelet therapy (e.g., prasugrel, ticagrelor).',                                                 '1A', 'CPIC'),
    (1, 'CYP2C19', '*2',  'Omeprazole',   'POOR_METABOLIZER', 'Decreased metabolism. Consider dose reduction for long-term use.',                                                    '2A', 'DPWG'),
    (1, 'CYP2C19', '*17', 'Clopidogrel',  'RAPID',            'Standard labeling. Enhanced platelet inhibition — increased bleeding risk.',                                           '1B', 'CPIC'),
    (1, 'CYP2C9',  '*2',  'Warfarin',     'INTERMEDIATE',     'Reduce initial dose by 25-50%. Monitor INR closely.',                                                                 '1A', 'CPIC'),
    (1, 'CYP2C9',  '*3',  'Warfarin',     'POOR_METABOLIZER', 'Reduce initial dose by 50-75%. High bleeding risk.',                                                                  '1A', 'CPIC'),
    (1, 'DPYD',    '*2A', 'Fluorouracil', 'POOR_METABOLIZER', 'Contraindicated. Use alternative chemotherapy. Life-threatening toxicity risk.',                                       '1A', 'CPIC'),
    (1, 'TPMT',    '*3A', 'Azathioprine', 'POOR_METABOLIZER', 'Reduce dose by 90% or use alternative. Risk of severe myelosuppression.',                                             '1A', 'CPIC'),
    (1, 'SLCO1B1', '*5',  'Simvastatin',  'INTERMEDIATE',     'Use lower dose (max 20mg) or alternative statin. Increased myopathy risk.',                                           '1A', 'CPIC'),
    (1, 'HLA-B',   '*5801','Allopurinol', 'POOR_METABOLIZER', 'Do NOT prescribe. High risk of severe cutaneous adverse reactions (SCAR).',                                           '1A', 'CPIC')
ON CONFLICT DO NOTHING;

-- ── 4. 공통코드 추가 ────────────────────────────────────────
INSERT INTO code_groups (group_key, group_name, use_yn, tenant_id) VALUES
    ('PGX_EFFECT',    'PGx 대사 표현형',    TRUE, 1),
    ('REPORT_STATUS', '보고서 상태',       TRUE, 1)
ON CONFLICT DO NOTHING;

INSERT INTO codes (group_key, code, name, value, sort_order, use_yn, tenant_id) VALUES
    ('PGX_EFFECT', 'POOR_METABOLIZER', 'Poor Metabolizer',     'POOR_METABOLIZER', 1, TRUE, 1),
    ('PGX_EFFECT', 'INTERMEDIATE',     'Intermediate',         'INTERMEDIATE',     2, TRUE, 1),
    ('PGX_EFFECT', 'NORMAL',           'Normal Metabolizer',   'NORMAL',           3, TRUE, 1),
    ('PGX_EFFECT', 'RAPID',            'Rapid/Ultra-rapid',    'RAPID',            4, TRUE, 1),
    ('REPORT_STATUS', 'DRAFT',  'Draft',  'DRAFT',  1, TRUE, 1),
    ('REPORT_STATUS', 'FINAL',  'Final',  'FINAL',  2, TRUE, 1)
ON CONFLICT DO NOTHING;

-- ── 5. 메뉴 추가 ───────────────────────────────────────────
INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES
    ((SELECT menu_id FROM menus WHERE name='Genomics' AND tenant_id=1 LIMIT 1),
     'Reports', '/genomics/reports', 'file-text', 4, TRUE, 'MENU', 1),
    ((SELECT menu_id FROM menus WHERE name='Genomics' AND tenant_id=1 LIMIT 1),
     'PGx', '/genomics/pgx', 'pill', 5, TRUE, 'MENU', 1);

INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, r.role_key
FROM menus m
CROSS JOIN (VALUES ('ADMIN'), ('USER')) AS r(role_key)
WHERE m.tenant_id = 1 AND m.name IN ('Reports', 'PGx')
ON CONFLICT DO NOTHING;
