# approval — 공통 결재 시스템

## 개요

`approval_code` (결재 기능 코드) 기반의 멀티테넌트 공통 결재 엔진.
업무 화면은 `approval_code`만 넘기면 정책 + 기본 결재선 + 주관부서가 자동 세팅되어 상신됨.

## 구조

```
approval/
├── controller/
│   ├── AdminApprovalDefinitionController  # /api/admin/approval/definitions (ADMIN)
│   ├── ApprovalLineTemplateController     # /api/approval/lines (사용자 개인)
│   └── ApprovalDocumentController         # /api/approval/documents (결재 요청/승인/조회)
├── service/
│   ├── ApprovalDefinitionService          # 결재 정책 CRUD + 권한 규칙
│   ├── ApprovalLineTemplateService        # 개인 결재선 양식 CRUD
│   └── ApprovalDocumentService            # 상신/승인/반려/회수/조회
├── mapper/                                # MyBatis 인터페이스
└── dto/
    ├── ApprovalDefinitionDtos.java        # Definition/AuthorityRule 관련
    ├── ApprovalLineTemplateDtos.java      # Template/Step 관련
    └── ApprovalDocumentDtos.java          # Document/Step/History + PopupInit
```

## 테이블 (V16)

| 테이블 | 역할 | tenant_id |
|--------|------|-----------|
| `approval_definition` | 결재 기능 코드 마스터 | ✓ |
| `approval_authority_rule` | 부서/역할 결재 권한 규칙 | ✓ |
| `approval_line_template` | 개인 결재선 양식 헤더 | ✓ |
| `approval_line_template_step` | 양식 단계 | (via template) |
| `approval_document` | 결재 문서 | ✓ |
| `approval_document_step` | 결재 단계 (status 전환) | (via doc) |
| `approval_history` | 이력 (감사/추적) | ✓ |

## 핵심 로직

### 결재 요청 흐름 (ApprovalDocumentService.request)

1. `approval_code` 로 `ApprovalDefinition` 조회 (활성 상태 확인)
2. 단계 결정:
   - `templateId` 지정 시 → 해당 양식의 단계 사용
   - 없으면 `req.steps` 직접 사용
3. 주관부서: 요청값 > 정책 기본값
4. `target_department_type` 에 따라 부서 ID 치환:
   - `REQUEST` → 요청자의 `org_id`
   - `SUPERVISING` → 주관부서 ID
   - `CUSTOM` → 요청의 `target_department_id`
5. 문서번호 생성: `AP-{tenantId}-{YYYYMM}-{seq}` (seq는 해당 월 최대값 +1)
6. `approval_document` + `approval_document_step` insert
7. `approval_history` 에 `REQUEST` 액션 기록

### 그룹결재 동시성 제어 (approve/reject)

**상태 조건부 UPDATE 방식** — 섹션 11.1 요구사항 대응.

```sql
UPDATE approval_document_step
   SET status = 'APPROVED', acted_by_user_id = ?, acted_at = NOW(), comment = ?
 WHERE step_id = ? AND document_id = ? AND status = 'PENDING'
```

- 영향받은 행 수가 0이면 → 다른 사용자가 먼저 처리한 것 → `AppException(CONFLICT, "이미 처리된 단계입니다.")`
- 비관적 락/낙관적 락 대비 단순하고 충돌 UI 불필요
- DB 레벨에서 보장되므로 race condition 없음

### 결재 완료 판정

`approve()` 후:
1. `findNextPendingStep(documentId)` 조회
2. 있으면 → document.status = `IN_PROGRESS`, current_step_order = next.step_order
3. 없으면 → document.status = `APPROVED`, completed_at = now

### 반려 (MVP)

즉시 종료 방식: document.status = `REJECTED`, 이후 단계는 PENDING 그대로 남음.
(2차 확장: 이전 단계 복귀, 재상신 등)

## API 엔드포인트

### 관리자 (/api/admin/approval/definitions)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | `/` | 정책 목록 | ADMIN |
| GET | `/{id}` | 정책 상세 (+ 권한규칙) | ADMIN |
| POST | `/` | 정책 생성 | ADMIN + CREATE |
| PUT | `/{id}` | 정책 수정 | ADMIN + EDIT |
| DELETE | `/{id}` | 정책 삭제 | ADMIN + DELETE |
| POST | `/{code}/authorities` | 권한 규칙 추가 | ADMIN + MANAGE |
| DELETE | `/authorities/{ruleId}` | 권한 규칙 삭제 | ADMIN + MANAGE |

### 개인 결재선 (/api/approval/lines)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | `/` | 내 양식 목록 | USER |
| GET | `/{id}` | 내 양식 상세 | USER |
| POST | `/` | 양식 생성 | MY_APPROVAL_LINE + CREATE |
| PUT | `/{id}` | 양식 수정 | MY_APPROVAL_LINE + EDIT |
| DELETE | `/{id}` | 양식 삭제 | MY_APPROVAL_LINE + DELETE |

### 결재 문서 (/api/approval/documents)
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | `/popup-init?approvalCode=X` | 팝업 초기 데이터 | USER |
| POST | `/` | 결재 상신 | APPROVAL_DOCUMENT + REQUEST |
| GET | `/{id}` | 문서 상세 | USER |
| GET | `/?inbox=...` | 결재함 (requested/pending/processed) | USER |
| POST | `/{id}/steps/{stepId}/approve` | 승인 | APPROVAL_DOCUMENT + APPROVE |
| POST | `/{id}/steps/{stepId}/reject` | 반려 | APPROVAL_DOCUMENT + REJECT |
| POST | `/{id}/withdraw` | 회수 (요청자 본인만) | APPROVAL_DOCUMENT + WITHDRAW |

## 프론트 통합

### 업무 화면에서 결재 호출 패턴

```tsx
import ApprovalRequestDialog from "@/components/approval/ApprovalRequestDialog";

const [open, setOpen] = useState(false);

<Button onClick={() => setOpen(true)}>결재 상신</Button>

<ApprovalRequestDialog
  open={open}
  onOpenChange={setOpen}
  approvalCode="PURCHASE_APPROVAL"
  businessType="PURCHASE"
  businessId={purchaseId}
  defaultTitle={`구매요청 - ${itemName}`}
  defaultBody={itemDetails}
  onSuccess={(docId) => toast.success("상신 #" + docId)}
/>
```

## 확장 포인트 (2차/3차)

- **첨부파일**: 기존 `files` 테이블 + `approval_document_files` 매핑 테이블 추가
- **알림**: 내 차례 도래 시 `notifications` 테이블에 insert (기존 알림 시스템 재사용)
- **회수/재상신**: `withdraw` 후 동일 `business_id` 로 새 문서 생성 로직
- **합의/검토**: `approval_type` 에 `REVIEW`, `CONSENT` 추가 + Service 분기
- **병렬 결재**: `step_order` 동일값 허용 + "모든 PENDING APPROVED" 조건
- **위임/대결**: `approval_document_step` 에 `delegated_from_user_id` 추가
- **반려 시 이전 단계 복귀**: `reject()` 로직을 `previous step PENDING으로 되돌림` 옵션으로 전환

## 주의사항

- 모든 insert 후 ID 필요 시 `currvalXxxId()` 호출 (MyBatis `<selectKey>` + @Param 조합 회피)
- `approval_authority_rule` 은 현재 조회용 (그룹결재 대상 판별은 단순 `target_department_id + target_role_key` 로 처리). 복잡한 권한 규칙 적용 시 `ApprovalDocumentMapper.countActableUsers` 에서 확장
- `target_department_type = REQUEST/SUPERVISING` 일 때 실제 `target_department_id` 는 상신 시점에 치환 저장 (이후 조직 개편에 영향받지 않음)
