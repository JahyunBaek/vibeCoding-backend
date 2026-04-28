package com.example.commonsystem.approval.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.DefinitionDetail;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.RequiredStepRow;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.ActionRequest;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.DocumentDetail;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.DocumentListRow;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.DocumentStepRow;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.ListQuery;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.PopupInitResponse;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.RequestApprovalRequest;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.StepRequest;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.StepRow;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.TemplateDetail;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.TemplateListRow;
import com.example.commonsystem.approval.mapper.ApprovalDocumentMapper;
import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.security.UserPrincipal;

@Service
public class ApprovalDocumentService {

  private static final DateTimeFormatter YYYYMM =
      DateTimeFormatter.ofPattern("yyyyMM").withZone(ZoneOffset.UTC);

  private final ApprovalDocumentMapper mapper;
  private final ApprovalDefinitionService definitionService;
  private final ApprovalLineTemplateService templateService;
  private final TenantContextHolder tenantCtx;

  public ApprovalDocumentService(ApprovalDocumentMapper mapper,
                                   ApprovalDefinitionService definitionService,
                                   ApprovalLineTemplateService templateService,
                                   TenantContextHolder tenantCtx) {
    this.mapper = mapper;
    this.definitionService = definitionService;
    this.templateService = templateService;
    this.tenantCtx = tenantCtx;
  }

  // --- 팝업 초기화 ---
  public PopupInitResponse popupInit(String approvalCode, UserPrincipal user) {
    DefinitionDetail def = definitionService.getByCode(approvalCode);
    if (!def.isActiveYn()) {
      throw new AppException(ErrorCode.VALIDATION, "비활성화된 결재 정책입니다.");
    }
    List<TemplateListRow> templates = templateService.myTemplates(user.getUserId(), approvalCode);
    TemplateDetail defaultTpl = templateService.defaultTemplate(user.getUserId(), approvalCode);

    List<StepRow> previewSteps = new ArrayList<>();
    if (defaultTpl != null && defaultTpl.steps() != null) {
      previewSteps.addAll(defaultTpl.steps());
    }

    List<RequiredStepRow> requiredSteps = def.getRequiredSteps() != null
        ? def.getRequiredSteps()
        : List.of();

    return new PopupInitResponse(def, defaultTpl, templates, previewSteps, requiredSteps);
  }

  // --- 결재 요청 (상신) ---
  @Transactional
  public long request(RequestApprovalRequest req, UserPrincipal user) {
    long tid = requireTenant();
    DefinitionDetail def = definitionService.getByCode(req.approvalCode());
    if (!def.isActiveYn()) {
      throw new AppException(ErrorCode.VALIDATION, "비활성화된 결재 정책입니다.");
    }

    // 사용자 단계 (양식 또는 직접 구성)
    List<StepRequest> userSteps = resolveSteps(req, user);

    // 정책 필수 단계 — 사용자 단계 뒤에 강제로 합쳐짐
    List<StepRequest> requiredSteps = toStepRequests(def.getRequiredSteps());

    List<StepRequest> finalSteps = new ArrayList<>(userSteps.size() + requiredSteps.size());
    finalSteps.addAll(userSteps);
    finalSteps.addAll(requiredSteps);

    if (finalSteps.isEmpty()) {
      throw new AppException(ErrorCode.VALIDATION, "결재 단계가 비어있습니다.");
    }

    // 주관부서 결정: 요청값 > 기본값 (주관부서 사용 기능만)
    Long supervisingDeptId = null;
    if (def.isUseSupervisingDepartment()) {
      supervisingDeptId = req.supervisingDepartmentId() != null
          ? req.supervisingDepartmentId()
          : def.getDefaultSupervisingDepartmentId();
      if (supervisingDeptId == null) {
        throw new AppException(ErrorCode.VALIDATION, "주관부서가 지정되지 않았습니다.");
      }
    }

    // 문서번호 생성
    String documentNo = nextDocumentNo(tid);

    // 문서 insert
    mapper.insertDocument(
        tid, documentNo, req.approvalCode(),
        req.businessType(), req.businessId(),
        req.title(), req.body(),
        user.getUserId(),
        user.getOrgId(),
        supervisingDeptId
    );
    long documentId = mapper.currvalDocumentId();

    // 단계 insert (REQUEST/SUPERVISING 타입은 실제 부서 ID로 치환)
    int order = 1;
    for (StepRequest s : finalSteps) {
      StepRequest materialized = materializeStep(s, user.getOrgId(), supervisingDeptId, order++);
      mapper.insertStep(documentId, materialized);
    }

    // 이력
    mapper.insertHistory(tid, documentId, null, "REQUEST", user.getUserId(), null, "DRAFT", "IN_PROGRESS");
    return documentId;
  }

  private List<StepRequest> resolveSteps(RequestApprovalRequest req, UserPrincipal user) {
    if (req.templateId() != null) {
      TemplateDetail tpl = templateService.detail(req.templateId(), user.getUserId());
      List<StepRequest> out = new ArrayList<>();
      for (StepRow r : tpl.steps()) {
        out.add(new StepRequest(
            r.stepOrder(), r.stepName(), r.approvalType(),
            r.targetDepartmentType(), r.targetDepartmentId(), r.targetRoleKey(),
            r.targetUserId(),
            r.groupApprovalYn(), r.requiredYn()
        ));
      }
      return out;
    }
    return req.steps() != null ? req.steps() : List.of();
  }

  /** 정책 RequiredStepRow → StepRequest 변환 */
  private List<StepRequest> toStepRequests(List<RequiredStepRow> rows) {
    if (rows == null || rows.isEmpty()) return List.of();
    List<StepRequest> out = new ArrayList<>(rows.size());
    for (RequiredStepRow r : rows) {
      out.add(new StepRequest(
          r.getStepOrder(), r.getStepName(), r.getApprovalType(),
          r.getTargetDepartmentType(), r.getTargetDepartmentId(), r.getTargetRoleKey(),
          r.getTargetUserId(),
          r.isGroupApprovalYn(), Boolean.TRUE
      ));
    }
    return out;
  }

  private StepRequest materializeStep(StepRequest s, Long requesterDeptId, Long supervisingDeptId, int order) {
    Long deptId = s.targetDepartmentId();
    String type = s.targetDepartmentType();
    // REQUEST/SUPERVISING은 동적으로 부서 ID 치환, CUSTOM/USER는 입력값 그대로 사용
    if ("REQUEST".equals(type)) {
      deptId = requesterDeptId;
    } else if ("SUPERVISING".equals(type)) {
      deptId = supervisingDeptId;
    }
    return new StepRequest(order, s.stepName(), s.approvalType(),
        type, deptId, s.targetRoleKey(),
        s.targetUserId(),
        s.groupApprovalYn() != null ? s.groupApprovalYn() : Boolean.TRUE,
        s.requiredYn() != null ? s.requiredYn() : Boolean.TRUE);
  }

  private String nextDocumentNo(long tenantId) {
    String yyyymm = YYYYMM.format(Instant.now());
    Integer maxSeq = mapper.findMaxDocumentSeq(tenantId, yyyymm);
    int next = (maxSeq == null ? 0 : maxSeq) + 1;
    return String.format("AP-%d-%s-%04d", tenantId, yyyymm, next);
  }

  // --- 승인 ---
  @Transactional
  public void approve(long documentId, long stepId, ActionRequest req, UserPrincipal user) {
    long tid = requireTenant();

    if (mapper.countActableUsers(stepId, user.getUserId()) == 0) {
      throw new AppException(ErrorCode.FORBIDDEN, "이 단계를 처리할 권한이 없습니다.");
    }

    int updated = mapper.actOnStepIfPending(stepId, documentId, "APPROVED", user.getUserId(), req.comment());
    if (updated == 0) {
      throw new AppException(ErrorCode.CONFLICT, "이미 처리된 단계입니다.");
    }

    // 다음 단계 확인
    DocumentStepRow next = mapper.findNextPendingStep(documentId);
    if (next != null) {
      mapper.updateDocumentStatus(documentId, tid, "IN_PROGRESS", next.stepOrder(), null);
    } else {
      mapper.updateDocumentStatus(documentId, tid, "APPROVED", null, Instant.now());
    }
    mapper.insertHistory(tid, documentId, stepId, "APPROVE", user.getUserId(), req.comment(),
        "IN_PROGRESS", next != null ? "IN_PROGRESS" : "APPROVED");
  }

  // --- 반려 (MVP: 즉시 종료) ---
  @Transactional
  public void reject(long documentId, long stepId, ActionRequest req, UserPrincipal user) {
    long tid = requireTenant();
    if (mapper.countActableUsers(stepId, user.getUserId()) == 0) {
      throw new AppException(ErrorCode.FORBIDDEN, "이 단계를 처리할 권한이 없습니다.");
    }
    int updated = mapper.actOnStepIfPending(stepId, documentId, "REJECTED", user.getUserId(), req.comment());
    if (updated == 0) {
      throw new AppException(ErrorCode.CONFLICT, "이미 처리된 단계입니다.");
    }
    mapper.updateDocumentStatus(documentId, tid, "REJECTED", null, Instant.now());
    mapper.insertHistory(tid, documentId, stepId, "REJECT", user.getUserId(), req.comment(),
        "IN_PROGRESS", "REJECTED");
  }

  // --- 회수 (요청자 본인, IN_PROGRESS 상태일 때) ---
  @Transactional
  public void withdraw(long documentId, UserPrincipal user) {
    long tid = requireTenant();
    int n = mapper.cancelOrWithdraw(documentId, tid, "IN_PROGRESS", "WITHDRAWN", user.getUserId(), Instant.now());
    if (n == 0) {
      throw new AppException(ErrorCode.CONFLICT, "회수할 수 없는 상태입니다.");
    }
    mapper.insertHistory(tid, documentId, null, "WITHDRAW", user.getUserId(), null, "IN_PROGRESS", "WITHDRAWN");
  }

  // --- 상세 ---
  public DocumentDetail detail(long documentId) {
    long tid = requireTenant();
    DocumentDetail d = mapper.findDetail(documentId, tid);
    if (d == null) throw new AppException(ErrorCode.NOT_FOUND, "결재 문서를 찾을 수 없습니다.");
    return d;
  }

  // --- 목록 ---
  public PageResponse<DocumentListRow> list(ListQuery q, UserPrincipal user) {
    Long tid = tenantCtx.resolveTenantId(null);
    if (tid == null) {
      // SUPER_ADMIN은 개인 결재함 없음
      return new PageResponse<>(Collections.emptyList(), Math.max(q.page(), 1), Math.min(Math.max(q.size(), 1), 100), 0);
    }
    int p = Math.max(q.page(), 1);
    int s = Math.min(Math.max(q.size(), 1), 100);
    int offset = (p - 1) * s;

    String inbox = q.inbox() == null ? "requested" : q.inbox();

    long total = mapper.countInbox(tid, user.getUserId(), inbox, q.approvalCode(), q.status(),
        q.keyword(), q.fromDate(), q.toDate());
    List<DocumentListRow> items = mapper.findInbox(tid, user.getUserId(), inbox, q.approvalCode(), q.status(),
        q.keyword(), q.fromDate(), q.toDate(), s, offset);
    return new PageResponse<>(items, p, s, total);
  }

  /** 결재 문서는 사용자 개인 데이터이므로 tenantId 필수. SUPER_ADMIN은 사용 불가. */
  private long requireTenant() {
    Long tid = tenantCtx.resolveTenantId(null);
    if (tid == null) {
      throw new AppException(ErrorCode.VALIDATION,
          "SUPER_ADMIN은 개인 결재함을 사용할 수 없습니다. 일반 사용자로 로그인하세요.");
    }
    return tid;
  }
}
