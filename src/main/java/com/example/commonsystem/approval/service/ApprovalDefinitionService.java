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
