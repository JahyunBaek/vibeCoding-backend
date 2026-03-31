# code — 공통코드 (드롭다운 데이터)

## 용도

UI의 드롭다운/콤보박스/필터 선택 항목을 서버에서 관리한다. Redis 캐시 기반.

## CodeService 주요 메서드

```java
// 캐시 조회 (프론트 호출용) — Redis 먼저 확인, 없으면 DB → Redis 저장
List<CodeItem> getCodesCached(String groupKey)

// 관리자 CRUD
void createGroup(groupKey, groupName, useYn)
void createItem(groupKey, code, name, value, sortOrder, useYn)
// update, delete...

// 캐시 무효화 (CUD 후 자동 호출됨)
void evict(Long tenantId, String groupKey)
```

## Redis 캐시 구조

키: `codes:{tenantId}:{groupKey}`, TTL 24h

## 등록된 그룹

| Group Key | 용도 |
|-----------|------|
| `YN` | 범용 Y/N |
| `PATIENT_STATUS` | 환자 상태 |
| `DEPARTMENT` | 진료과 |
| `BLOOD_TYPE` | 혈액형 |
| `GENDER` | 성별 |
| `TRIAL_PHASE` | 임상시험 단계 |
| `TRIAL_STATUS` | 임상시험 상태 |

## 새 공통코드 추가 절차

1. Flyway 마이그레이션에 `code_groups` + `codes` INSERT (tenant_id = 0, 1 모두)
2. `TenantService.provisionTenant()`에 `insertCodeGroup()` + `insertCode()` 추가
3. 프론트에서 `api.commonCodes("GROUP_KEY")`로 사용
