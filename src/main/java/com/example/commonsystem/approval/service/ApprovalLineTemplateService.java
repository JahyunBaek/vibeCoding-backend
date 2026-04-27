package com.example.commonsystem.approval.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.CreateRequest;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.StepRequest;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.TemplateDetail;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.TemplateListRow;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.UpdateRequest;
import com.example.commonsystem.approval.mapper.ApprovalLineTemplateMapper;
import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.common.exception.AppException;

@Service
public class ApprovalLineTemplateService {

  private final ApprovalLineTemplateMapper mapper;
  private final TenantContextHolder tenantCtx;

  public ApprovalLineTemplateService(ApprovalLineTemplateMapper mapper, TenantContextHolder tenantCtx) {
    this.mapper = mapper;
    this.tenantCtx = tenantCtx;
  }

  public List<TemplateListRow> myTemplates(long userId, String approvalCode) {
    Long tid = tenantCtx.resolveTenantId(null);
    if (tid == null) return Collections.emptyList(); // SUPER_ADMIN은 개인 양식 없음
    return mapper.findMyTemplates(tid, userId, approvalCode);
  }

  public TemplateDetail detail(long templateId, long userId) {
    long tid = requireTenant();
    TemplateDetail d = mapper.findDetail(templateId, tid, userId);
    if (d == null) throw new AppException(ErrorCode.NOT_FOUND, "결재선 양식을 찾을 수 없습니다.");
    return d;
  }

  public TemplateDetail defaultTemplate(long userId, String approvalCode) {
    Long tid = tenantCtx.resolveTenantId(null);
    if (tid == null) return null; // SUPER_ADMIN은 기본 양식 없음
    return mapper.findDefault(tid, userId, approvalCode);
  }

  @Transactional
  public long create(CreateRequest req, long userId) {
    long tid = requireTenant();
    validateSteps(req.steps());

    if (req.defaultYn()) {
      mapper.unsetDefault(tid, userId, req.approvalCode());
    }
    mapper.insertTemplate(tid, userId, req.approvalCode(), req.templateName(), req.defaultYn());
    long templateId = mapper.currvalTemplateId();
    for (StepRequest s : req.steps()) {
      mapper.insertStep(templateId, s);
    }
    return templateId;
  }

  @Transactional
  public void update(long templateId, UpdateRequest req, long userId) {
    long tid = requireTenant();
    validateSteps(req.steps());
    TemplateDetail existing = mapper.findDetail(templateId, tid, userId);
    if (existing == null) throw new AppException(ErrorCode.NOT_FOUND, "결재선 양식을 찾을 수 없습니다.");

    if (req.defaultYn()) {
      mapper.unsetDefault(tid, userId, existing.approvalCode());
    }
    mapper.updateTemplate(templateId, tid, userId, req.templateName(), req.defaultYn(), req.activeYn());
    mapper.deleteStepsByTemplate(templateId);
    for (StepRequest s : req.steps()) {
      mapper.insertStep(templateId, s);
    }
  }

  @Transactional
  public void delete(long templateId, long userId) {
    long tid = requireTenant();
    int n = mapper.deleteTemplate(templateId, tid, userId);
    if (n == 0) throw new AppException(ErrorCode.NOT_FOUND, "결재선 양식을 찾을 수 없습니다.");
  }

  /** 결재선 양식은 사용자 개인 데이터이므로 tenantId 필수. SUPER_ADMIN은 사용 불가. */
  private long requireTenant() {
    Long tid = tenantCtx.resolveTenantId(null);
    if (tid == null) {
      throw new AppException(ErrorCode.VALIDATION,
          "SUPER_ADMIN은 개인 결재선 양식을 사용할 수 없습니다. 일반 사용자로 로그인하세요.");
    }
    return tid;
  }

  private void validateSteps(List<StepRequest> steps) {
    if (steps == null || steps.isEmpty()) {
      throw new AppException(ErrorCode.VALIDATION, "최소 1개 이상의 결재 단계가 필요합니다.");
    }
    for (StepRequest s : steps) {
      if (s.stepOrder() < 1) {
        throw new AppException(ErrorCode.VALIDATION, "step_order는 1 이상이어야 합니다.");
      }
      String type = s.targetDepartmentType();
      if (!"REQUEST".equals(type) && !"SUPERVISING".equals(type)
          && !"CUSTOM".equals(type) && !"USER".equals(type)) {
        throw new AppException(ErrorCode.VALIDATION, "target_department_type 값이 올바르지 않습니다: " + type);
      }
      if ("CUSTOM".equals(type) && s.targetDepartmentId() == null) {
        throw new AppException(ErrorCode.VALIDATION, "CUSTOM 타입 단계는 target_department_id가 필요합니다.");
      }
      if ("USER".equals(type) && s.targetUserId() == null) {
        throw new AppException(ErrorCode.VALIDATION, "USER 타입 단계는 target_user_id가 필요합니다.");
      }
    }
  }
}
