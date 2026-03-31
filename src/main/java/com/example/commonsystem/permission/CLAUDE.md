# permission — 화면-액션 권한 시스템

## 구조

- `screens` (글로벌) → `screen_actions` (글로벌) → `role_actions` (테넌트별)
- 화면/액션 정의는 전체 공유, 역할 배정만 테넌트별 격리

## 권한 체크 — @RequiresAction

컨트롤러 메서드에 선언적 권한 체크. AOP(`ActionPermissionAspect`)가 자동 실행.

```java
@RequiresAction(screen = "BOARD_POST", action = "CREATE")
@PostMapping
public ApiResponse<Long> create(...) { ... }
```

- SUPER_ADMIN은 자동 통과
- 권한 없으면 `AppException(FORBIDDEN)` 발생

## PermissionService 주요 메서드

```java
// 현재 사용자의 모든 권한 조회 (프론트 초기화용)
List<ScreenActionDto> myPermissions()

// 특정 권한 체크 (AOP 내부에서 호출)
void checkAction(String screen, String action)

// 역할-액션 매핑 조회/설정 (Admin 화면)
List<String> rolesByAction(long actionId)
void setRoleActions(long actionId, List<String> roleKeys)
```

## 새 화면/액션 추가 절차

1. Flyway 마이그레이션에 `screens` + `screen_actions` INSERT
2. `role_actions`에 기본 매핑 INSERT (tenant_id = 0, 1)
3. `TenantService.provisionTenant()`에도 추가 (신규 테넌트 자동 생성)
4. 컨트롤러에 `@RequiresAction` 적용
5. 프론트 `src/config/permissions.ts`에 상수 추가
