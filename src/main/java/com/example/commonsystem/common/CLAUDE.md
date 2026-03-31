# common — 공통 유틸리티 & 인프라

새 도메인을 만들 때 여기 있는 것을 반드시 재사용할 것. 직접 구현하지 않는다.

## 응답 래퍼

### ApiResponse\<T\>

모든 엔드포인트의 표준 응답 형식. 직접 Map이나 다른 형태로 반환하지 않는다.

```java
// 성공 (데이터 있음)
return ApiResponse.ok(data);

// 성공 (데이터 없음)
return ApiResponse.ok();

// 실패 — 직접 사용하지 않고 AppException을 throw
return ApiResponse.fail(ErrorCode.NOT_FOUND, "리소스를 찾을 수 없습니다");
```

### PageResponse\<T\>

페이징 응답은 반드시 이 record를 사용한다.

```java
return ApiResponse.ok(new PageResponse<>(items, page, size, total));
```

### ApiError

`ApiResponse.error` 필드의 타입. `{ code, message }` 구조.

## 에러 처리

### ErrorCode

에러 코드 상수. 새 코드를 임의로 만들지 않고 여기서 선택한다.

| 상수 | 값 | HTTP Status |
|------|------|-------------|
| `UNAUTHORIZED` | AUTH_401 | 401 |
| `FORBIDDEN` | AUTH_403 | 403 |
| `NOT_FOUND` | COMMON_404 | 400 |
| `VALIDATION` | COMMON_400 | 400 |
| `CONFLICT` | COMMON_409 | 409 |
| `INTERNAL` | COMMON_500 | 500 |

### AppException

서비스에서 에러 발생 시 throw한다. `GlobalExceptionHandler`가 자동으로 `ApiResponse.fail()`로 변환.

```java
throw new AppException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다");
throw new AppException(ErrorCode.FORBIDDEN, "권한이 없습니다");
throw new AppException(ErrorCode.CONFLICT, "이미 존재하는 키입니다");
```

### GlobalExceptionHandler

`@RestControllerAdvice`. 다음 예외를 자동 처리한다:
- `AppException` → HTTP 400/409 + `ApiResponse.fail()`
- `MethodArgumentNotValidException` → 필드 검증 에러 메시지
- `BindException`, `ConstraintViolationException` → 제약조건 위반
- `Exception` → HTTP 500 + 로깅

## 멀티테넌시

### TenantContextHolder

현재 요청의 테넌트 컨텍스트를 추출한다. **새 서비스에서 tenantId가 필요하면 반드시 이것을 사용.**

```java
// 현재 사용자의 tenantId (SUPER_ADMIN이면 null)
Long tid = TenantContextHolder.currentTenantId();

// SUPER_ADMIN이 특정 테넌트를 지정하거나, 일반 사용자는 자기 테넌트
Long tid = TenantContextHolder.resolveTenantId(overrideTenantId);

// SUPER_ADMIN 여부
boolean isSA = TenantContextHolder.isSuperAdmin();

// UserPrincipal 직접 접근
UserPrincipal p = TenantContextHolder.currentPrincipal();
```

## 유틸리티 서비스

### RateLimitService

Redis 기반 슬라이딩 윈도우 Rate Limit. 새 엔드포인트에 속도 제한이 필요하면 재사용.

```java
@Autowired RateLimitService rateLimitService;

// key당 limit회, window초 동안
boolean allowed = rateLimitService.tryAcquire("login:" + ip, 5, 600);
if (!allowed) throw new AppException(ErrorCode.VALIDATION, "Too many requests");
```

Redis 키: `rate:{key}`, TTL = window

### IdempotencyService

Redis 기반 멱등성 키. 중복 요청 방지에 사용.

```java
@Autowired IdempotencyService idempotencyService;

// true면 신규, false면 중복
boolean isNew = idempotencyService.tryConsume(idempotencyKey);
```

Redis 키: `idempotency:{key}`, TTL 24h

### CsvExportService

Excel 호환 CSV 생성. UTF-8 BOM 포함.

```java
@Autowired CsvExportService csvExportService;

String csv = csvExportService.toCsv(
    List.of("이름", "이메일", "역할"),       // headers
    rows.stream().map(r -> List.of(r.name(), r.email(), r.role())).toList()  // rows
);
// 컨트롤러에서 ResponseEntity<byte[]>로 반환
```

### I18nService

테넌트 locale에 따른 메시지 번역. 이메일, 알림, CSV 헤더 등에 사용.

```java
@Autowired I18nService i18nService;

Locale locale = i18nService.toLocale("ko");
String msg = i18nService.getMessage("email.reset.subject", locale);
String msg2 = i18nService.getMessage("email.reset.body", locale, userName, resetUrl);
```

메시지 파일: `src/main/resources/messages.properties`, `messages_ko.properties`, `messages_en.properties`

### EmailService

HTML 이메일 발송. 메일 전송이 필요하면 직접 JavaMailSender를 쓰지 않고 이것을 사용.

```java
@Autowired EmailService emailService;

// 미리 만들어진 템플릿
emailService.sendPasswordReset(toEmail, userName, resetToken, locale);
emailService.sendInvitation(toEmail, tenantName, inviteToken, locale);

// 범용 HTML 이메일
emailService.sendGeneric(to, subject, htmlBody);
```

`app.mail.enabled=false`이면 실제 발송 대신 로그만 출력.
