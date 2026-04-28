package com.example.commonsystem.approval.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.CreateAuthorityRuleRequest;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.CreateRequest;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.DefinitionDetail;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.DefinitionListRow;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.UpdateRequest;
import com.example.commonsystem.approval.mapper.ApprovalDefinitionMapper;
import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.common.exception.AppException;

@Service
public class ApprovalDefinitionService {

  private final ApprovalDefinitionMapper mapper;
  private final TenantContextHolder tenantCtx;

  public ApprovalDefinitionService(ApprovalDefinitionMapper mapper, TenantContextHolder tenantCtx) {
    this.mapper = mapper;
    this.tenantCtx = tenantCtx;
  }

  // ── 조회는 null 허용 (SUPER_ADMIN이 테넌트 미선택 시 전체 조회) ──

  public List<DefinitionListRow> list(boolean activeOnly, String keyword, Long tenantIdOverride) {
    Long tid = tenantCtx.resolveTenantId(tenantIdOverride);
    return mapper.findAll(tid, activeOnly, keyword);
  }

  public DefinitionDetail get(long definitionId, Long tenantIdOverride) {
    Long tid = tenantCtx.resolveTenantId(tenantIdOverride);
    DefinitionDetail d = mapper.findById(definitionId, tid);
    if (d == null) throw new AppException(ErrorCode.NOT_FOUND, "결재 정책을 찾을 수 없습니다.");
    return d;
  }

  public DefinitionDetail getByCode(String approvalCode) {
    Long tid = tenantCtx.resolveTenantId(null);
    DefinitionDetail d = mapper.findByCode(tid, approvalCode);
    if (d == null) throw new AppException(ErrorCode.NOT_FOUND, "결재 정책을 찾을 수 없습니다.");
    return d;
  }

  // ── 쓰기 작업은 tenantId 필수 ──

  @Transactional
  public long create(CreateRequest req, long userId, Long tenantIdOverride) {
    long tid = requireTenant(tenantIdOverride);
    if (mapper.existsByCode(tid, req.approvalCode()) > 0) {
      throw new AppException(ErrorCode.CONFLICT, "이미 존재하는 결재 코드입니다: " + req.approvalCode());
    }
    mapper.insert(tid, req, userId);
    // PostgreSQL RETURNING 을 통해 definition_id는 MyBatis가 자동 할당하지 않음 → 재조회
    DefinitionDetail d = mapper.findByCode(tid, req.approvalCode());
    return d.getDefinitionId();
  }

  @Transactional
  public void update(long definitionId, UpdateRequest req, long userId, Long tenantIdOverride) {
    long tid = requireTenant(tenantIdOverride);
    int n = mapper.update(definitionId, tid, req, userId);
    if (n == 0) throw new AppException(ErrorCode.NOT_FOUND, "결재 정책을 찾을 수 없습니다.");
  }

  @Transactional
  public void delete(long definitionId, Long tenantIdOverride) {
    long tid = requireTenant(tenantIdOverride);
    int n = mapper.deleteById(definitionId, tid);
    if (n == 0) throw new AppException(ErrorCode.NOT_FOUND, "결재 정책을 찾을 수 없습니다.");
  }

  @Transactional
  public void addAuthorityRule(String approvalCode, CreateAuthorityRuleRequest req, Long tenantIdOverride) {
    long tid = requireTenant(tenantIdOverride);
    mapper.insertAuthorityRule(tid, approvalCode, req);
  }

  @Transactional
  public void deleteAuthorityRule(long ruleId, Long tenantIdOverride) {
    long tid = requireTenant(tenantIdOverride);
    int n = mapper.deleteAuthorityRule(ruleId, tid);
    if (n == 0) throw new AppException(ErrorCode.NOT_FOUND, "권한 규칙을 찾을 수 없습니다.");
  }

  // ── 필수 단계 (Required Steps) ──

  public List<com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.RequiredStepRow>
  requiredStepsByCode(String approvalCode) {
    Long tid = tenantCtx.resolveTenantId(null);
    if (tid == null) return List.of();
    return mapper.findRequiredStepsByCode(tid, approvalCode);
  }

  @Transactional
  public void addRequiredStep(long definitionId,
                                com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.CreateRequiredStepRequest req,
                                Long tenantIdOverride) {
    long tid = requireTenant(tenantIdOverride);
    if (mapper.countDefinitionByIdAndTenant(definitionId, tid) == 0) {
      throw new AppException(ErrorCode.NOT_FOUND, "결재 정책을 찾을 수 없습니다.");
    }
    validateRequiredStep(req);

    int order = (req.stepOrder() != null && req.stepOrder() > 0)
        ? req.stepOrder()
        : (mapper.findMaxRequiredStepOrder(definitionId) + 1);
    mapper.insertRequiredStep(definitionId, order, req);
  }

  @Transactional
  public void deleteRequiredStep(long definitionId, long requiredStepId, Long tenantIdOverride) {
    long tid = requireTenant(tenantIdOverride);
    if (mapper.countDefinitionByIdAndTenant(definitionId, tid) == 0) {
      throw new AppException(ErrorCode.NOT_FOUND, "결재 정책을 찾을 수 없습니다.");
    }
    int n = mapper.deleteRequiredStep(requiredStepId, definitionId);
    if (n == 0) throw new AppException(ErrorCode.NOT_FOUND, "필수 단계를 찾을 수 없습니다.");
  }

  private void validateRequiredStep(
      com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.CreateRequiredStepRequest req) {
    String type = req.targetDepartmentType();
    if (!"REQUEST".equals(type) && !"SUPERVISING".equals(type)
        && !"CUSTOM".equals(type) && !"USER".equals(type)) {
      throw new AppException(ErrorCode.VALIDATION,
          "target_department_type 값이 올바르지 않습니다: " + type);
    }
    if ("CUSTOM".equals(type) && req.targetDepartmentId() == null) {
      throw new AppException(ErrorCode.VALIDATION,
          "CUSTOM 타입 단계는 target_department_id가 필요합니다.");
    }
    if ("USER".equals(type) && req.targetUserId() == null) {
      throw new AppException(ErrorCode.VALIDATION,
          "USER 타입 단계는 target_user_id가 필요합니다.");
    }
  }

  /**
   * 쓰기 작업 시 tenantId가 반드시 필요. SUPER_ADMIN이 테넌트 미선택 상태라면 예외.
   */
  private long requireTenant(Long tenantIdOverride) {
    Long tid = tenantCtx.resolveTenantId(tenantIdOverride);
    if (tid == null) {
      throw new AppException(ErrorCode.VALIDATION, "테넌트를 선택해야 작업할 수 있습니다.");
    }
    return tid;
  }
}
