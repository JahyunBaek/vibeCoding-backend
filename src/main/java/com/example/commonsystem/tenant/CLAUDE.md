# tenant — 멀티테넌시 & SaaS

## 핵심 서비스

### TenantService — 프로비저닝

새 테넌트 생성 시 자동 초기화 항목:
1. `tenants` 테이블에 INSERT
2. 역할-액션 매핑 (`role_actions`) — ADMIN에 모든 액션 할당
3. 메뉴 트리 (Dashboard, Medical, Boards, Admin)
4. 기본 게시판 "공지사항" + 대응 메뉴
5. 공통코드 그룹/항목 (YN, PATIENT_STATUS, DEPARTMENT, BLOOD_TYPE, GENDER, TRIAL_PHASE, TRIAL_STATUS)
6. Admin 사용자 계정
7. 테넌트 설정 기본값 (company_name, logo_url, timezone, locale)

**새 공통코드/메뉴/권한 추가 시 반드시 이 메서드도 업데이트** — 기존 테넌트는 Flyway로, 신규 테넌트는 여기서 처리.

### TenantConfigService — 테넌트 설정

```java
List<TenantConfig> getAll(Long tenantIdOverride)
void saveAll(Map<String, String> configs, Long tenantIdOverride)
String getLocale(Long tenantId)   // "ko" or "en", 기본 "ko"
void initDefaults(Long tenantId, String tenantName)
```

### TenantContextHolder (common 패키지)

현재 요청의 tenantId를 추출. 상세 사용법은 `common/CLAUDE.md` 참조.

## 테넌트 격리 범위

| 격리됨 (tenant_id 있음) | 글로벌 (공유) |
|--------------------------|---------------|
| users, orgs, menus, boards, posts, comments | roles |
| code_groups, codes, files, audit_logs | screens, screen_actions |
| role_actions, tenant_configs, notifications | |
