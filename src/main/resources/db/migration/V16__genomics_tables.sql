-- ============================================================
-- V16: 유전체 분석 플랫폼 기반 테이블
--   - genomic_samples: 환자별 시퀀싱 샘플
--   - gene_panels: 유전자 패널 정의
--   - panel_genes: 패널-유전자 매핑
--   - variants: 변이 데이터 (SNV, InDel, CNV, SV)
--   - 메뉴 등록
-- ============================================================

-- ── 1. 유전체 샘플 ──────────────────────────────────────────
CREATE TABLE genomic_samples (
    sample_id       BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    patient_id      BIGINT       NOT NULL,
    sample_no       VARCHAR(30)  NOT NULL,           -- e.g. GS-2026-0001
    sample_type     VARCHAR(20)  NOT NULL DEFAULT 'BLOOD',  -- BLOOD, TISSUE, SALIVA, OTHER
    panel_id        BIGINT,                          -- FK → gene_panels (nullable: WGS는 패널 없음)
    status          VARCHAR(20)  NOT NULL DEFAULT 'RECEIVED',
        -- RECEIVED → EXTRACTED → SEQUENCING → ANALYZING → COMPLETED → REPORTED
    received_date   DATE         NOT NULL DEFAULT CURRENT_DATE,
    completed_date  DATE,
    note            TEXT,
    created_by      BIGINT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, sample_no)
);

CREATE INDEX idx_gs_tenant       ON genomic_samples (tenant_id);
CREATE INDEX idx_gs_patient      ON genomic_samples (tenant_id, patient_id);
CREATE INDEX idx_gs_status       ON genomic_samples (tenant_id, status);

COMMENT ON TABLE  genomic_samples              IS '유전체 분석 샘플';
COMMENT ON COLUMN genomic_samples.sample_type  IS '검체 유형: BLOOD, TISSUE, SALIVA, OTHER';
COMMENT ON COLUMN genomic_samples.status       IS '워크플로우: RECEIVED→EXTRACTED→SEQUENCING→ANALYZING→COMPLETED→REPORTED';

-- ── 2. 유전자 패널 ──────────────────────────────────────────
CREATE TABLE gene_panels (
    panel_id        BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    panel_code      VARCHAR(30)  NOT NULL,           -- e.g. ONCO-50, RARE-200
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    category        VARCHAR(30)  NOT NULL DEFAULT 'TARGETED',  -- TARGETED, WES, WGS
    gene_count      INT          NOT NULL DEFAULT 0,
    use_yn          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, panel_code)
);

CREATE INDEX idx_gp_tenant ON gene_panels (tenant_id);

COMMENT ON TABLE  gene_panels          IS '유전자 패널 정의';
COMMENT ON COLUMN gene_panels.category IS '패널 카테고리: TARGETED, WES, WGS';

-- ── 3. 패널-유전자 매핑 ─────────────────────────────────────
CREATE TABLE panel_genes (
    panel_gene_id   BIGSERIAL PRIMARY KEY,
    panel_id        BIGINT       NOT NULL REFERENCES gene_panels(panel_id) ON DELETE CASCADE,
    gene_symbol     VARCHAR(30)  NOT NULL,           -- e.g. BRCA1, TP53, EGFR
    chromosome      VARCHAR(5),                      -- e.g. chr17, chrX
    description     VARCHAR(200),
    UNIQUE (panel_id, gene_symbol)
);

CREATE INDEX idx_pg_panel ON panel_genes (panel_id);

COMMENT ON TABLE panel_genes IS '패널에 포함된 유전자 목록';

-- ── 4. 변이 데이터 ──────────────────────────────────────────
CREATE TABLE variants (
    variant_id      BIGSERIAL PRIMARY KEY,
    sample_id       BIGINT       NOT NULL REFERENCES genomic_samples(sample_id) ON DELETE CASCADE,
    tenant_id       BIGINT       NOT NULL,
    gene_symbol     VARCHAR(30)  NOT NULL,           -- e.g. BRCA1
    chromosome      VARCHAR(5)   NOT NULL,           -- e.g. chr17
    position        BIGINT       NOT NULL,           -- genomic position
    ref_allele      VARCHAR(500) NOT NULL,           -- reference allele
    alt_allele      VARCHAR(500) NOT NULL,           -- alternate allele
    variant_type    VARCHAR(10)  NOT NULL DEFAULT 'SNV',  -- SNV, INDEL, CNV, SV
    zygosity        VARCHAR(10),                     -- HET, HOM, HEMI
    quality         DOUBLE PRECISION,                -- QUAL score
    read_depth      INT,                             -- DP
    allele_freq     DOUBLE PRECISION,                -- VAF (0.0 ~ 1.0)

    -- 어노테이션
    hgvs_c          VARCHAR(200),                    -- c.5382insC
    hgvs_p          VARCHAR(200),                    -- p.Ser1794Ter
    consequence     VARCHAR(50),                     -- missense, nonsense, frameshift, ...
    impact          VARCHAR(10),                     -- HIGH, MODERATE, LOW, MODIFIER
    acmg_class      VARCHAR(30),                     -- PATHOGENIC, LIKELY_PATHOGENIC, VUS, LIKELY_BENIGN, BENIGN
    clinvar_id      VARCHAR(30),                     -- ClinVar accession
    gnomad_af       DOUBLE PRECISION,                -- gnomAD allele frequency
    cosmic_id       VARCHAR(30),                     -- COSMIC ID

    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_var_sample    ON variants (sample_id);
CREATE INDEX idx_var_tenant    ON variants (tenant_id);
CREATE INDEX idx_var_gene      ON variants (tenant_id, gene_symbol);
CREATE INDEX idx_var_acmg      ON variants (tenant_id, acmg_class);
CREATE INDEX idx_var_chr_pos   ON variants (chromosome, position);

COMMENT ON TABLE  variants            IS '검출 변이';
COMMENT ON COLUMN variants.acmg_class IS 'ACMG 5단계: PATHOGENIC, LIKELY_PATHOGENIC, VUS, LIKELY_BENIGN, BENIGN';

-- ── 5. 공통코드 등록 ────────────────────────────────────────
-- code_groups 등록
INSERT INTO code_groups (group_key, group_name, use_yn, tenant_id) VALUES
    ('SAMPLE_TYPE',    '검체 유형',       TRUE, 1),
    ('SAMPLE_STATUS',  '샘플 상태',       TRUE, 1),
    ('VARIANT_TYPE',   '변이 유형',       TRUE, 1),
    ('ACMG_CLASS',     'ACMG 분류',       TRUE, 1),
    ('PANEL_CATEGORY', '패널 카테고리',   TRUE, 1)
ON CONFLICT DO NOTHING;

-- codes 등록
INSERT INTO codes (group_key, code, name, value, sort_order, use_yn, tenant_id) VALUES
    -- 검체 유형
    ('SAMPLE_TYPE', 'BLOOD',   '혈액',   'BLOOD',   1, TRUE, 1),
    ('SAMPLE_TYPE', 'TISSUE',  '조직',   'TISSUE',  2, TRUE, 1),
    ('SAMPLE_TYPE', 'SALIVA',  '타액',   'SALIVA',  3, TRUE, 1),
    ('SAMPLE_TYPE', 'OTHER',   '기타',   'OTHER',   4, TRUE, 1),
    -- 샘플 상태
    ('SAMPLE_STATUS', 'RECEIVED',   '접수',     'RECEIVED',   1, TRUE, 1),
    ('SAMPLE_STATUS', 'EXTRACTED',  '추출완료', 'EXTRACTED',  2, TRUE, 1),
    ('SAMPLE_STATUS', 'SEQUENCING', '시퀀싱중', 'SEQUENCING', 3, TRUE, 1),
    ('SAMPLE_STATUS', 'ANALYZING',  '분석중',   'ANALYZING',  4, TRUE, 1),
    ('SAMPLE_STATUS', 'COMPLETED',  '분석완료', 'COMPLETED',  5, TRUE, 1),
    ('SAMPLE_STATUS', 'REPORTED',   '보고완료', 'REPORTED',   6, TRUE, 1),
    -- 변이 유형
    ('VARIANT_TYPE', 'SNV',   'SNV',   'SNV',   1, TRUE, 1),
    ('VARIANT_TYPE', 'INDEL', 'InDel', 'INDEL', 2, TRUE, 1),
    ('VARIANT_TYPE', 'CNV',   'CNV',   'CNV',   3, TRUE, 1),
    ('VARIANT_TYPE', 'SV',    'SV',    'SV',    4, TRUE, 1),
    -- ACMG 분류
    ('ACMG_CLASS', 'PATHOGENIC',        'Pathogenic',        'PATHOGENIC',        1, TRUE, 1),
    ('ACMG_CLASS', 'LIKELY_PATHOGENIC', 'Likely Pathogenic', 'LIKELY_PATHOGENIC', 2, TRUE, 1),
    ('ACMG_CLASS', 'VUS',              'VUS',               'VUS',               3, TRUE, 1),
    ('ACMG_CLASS', 'LIKELY_BENIGN',    'Likely Benign',     'LIKELY_BENIGN',     4, TRUE, 1),
    ('ACMG_CLASS', 'BENIGN',           'Benign',            'BENIGN',            5, TRUE, 1),
    -- 패널 카테고리
    ('PANEL_CATEGORY', 'TARGETED', 'Targeted Panel', 'TARGETED', 1, TRUE, 1),
    ('PANEL_CATEGORY', 'WES',      'Whole Exome',    'WES',      2, TRUE, 1),
    ('PANEL_CATEGORY', 'WGS',      'Whole Genome',   'WGS',      3, TRUE, 1)
ON CONFLICT DO NOTHING;

-- ── 6. 메뉴 등록 ────────────────────────────────────────────
-- 상위 그룹: Genomics
INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES (NULL, 'Genomics', NULL, 'dna', 7, TRUE, 'GROUP', 1);

-- 하위 메뉴: Samples, Panels, Variants
INSERT INTO menus (parent_id, name, path, icon, sort_order, use_yn, menu_type, tenant_id)
VALUES
    ((SELECT menu_id FROM menus WHERE name='Genomics' AND tenant_id=1 LIMIT 1),
     'Samples', '/genomics/samples', 'test-tubes', 1, TRUE, 'MENU', 1),
    ((SELECT menu_id FROM menus WHERE name='Genomics' AND tenant_id=1 LIMIT 1),
     'Panels', '/genomics/panels', 'layout-list', 2, TRUE, 'MENU', 1),
    ((SELECT menu_id FROM menus WHERE name='Genomics' AND tenant_id=1 LIMIT 1),
     'Variants', '/genomics/variants', 'scan-search', 3, TRUE, 'MENU', 1);

-- 역할 배정
INSERT INTO menu_roles (menu_id, role_key)
SELECT m.menu_id, r.role_key
FROM menus m
CROSS JOIN (VALUES ('ADMIN'), ('USER')) AS r(role_key)
WHERE m.tenant_id = 1
  AND m.name IN ('Genomics', 'Samples', 'Panels', 'Variants')
ON CONFLICT DO NOTHING;
