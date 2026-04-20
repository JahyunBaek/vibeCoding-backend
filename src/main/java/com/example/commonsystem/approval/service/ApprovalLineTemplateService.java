package com.example.commonsystem.approval.service;

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
    long tid = tenantCtx.resolveTenantId(null);
    return mapper.findMyTemplates(tid, userId, approvalCode);
  }

  public TemplateDetail detail(long templateId, long userId) {
    long tid = tenantCtx.resolveTenantId(null);
    TemplateDetail d = mapper.findDetail(templateId, tid, userId);
    if (d == null) throw new AppException(ErrorCode.NOT_FOUND, "결재선 양식을 찾을 수 없습니다.");
    return d;
  }

  public TemplateDetail defaultTemplate(long userId, String approvalCode) {
    long tid = tenantCtx.resolveTenantId(null);
    return mapper.findDefault(tid, userId, approvalCode);
  }

  @Transactional
  public long create(CreateRequest req, long userId) {
    long tid = tenantCtx.resolveTenantId(null);
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
    long tid = tenantCtx.resolveTenantId(null);
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
    long tid = tenantCtx.resolveTenantId(null);
    int n = mapper.deleteTemplate(templateId, tid, userId);
    if (n == 0) throw new AppException(ErrorCode.NOT_FOUND, "결재선 양식을 찾을 수 없습니다.");
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
      if (!"REQUEST".equals(type) && !"SUPERVISING".equals(type) && !"CUSTOM".equals(type)) {
        throw new AppException(ErrorCode.VALIDATION, "target_department_type 값이 올바르지 않습니다: " + type);
      }
      if ("CUSTOM".equals(type) && s.targetDepartmentId() == null) {
        throw new AppException(ErrorCode.VALIDATION, "CUSTOM 타입 단계는 target_department_id가 필요합니다.");
      }
    }
  }
}
