package com.example.commonsystem.audit.service;

import com.example.commonsystem.audit.domain.AuditLog;
import com.example.commonsystem.audit.mapper.AuditLogMapper;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditService {

  private final AuditLogMapper auditLogMapper;
  private final TenantContextHolder tenantCtx;

  public AuditService(AuditLogMapper auditLogMapper, TenantContextHolder tenantCtx) {
    this.auditLogMapper = auditLogMapper;
    this.tenantCtx = tenantCtx;
  }

  /** SecurityContext에서 사용자 정보를 자동으로 추출하여 로그 기록 */
  public void log(String action, String targetType, String targetId, String detail) {
    UserPrincipal p = tenantCtx.currentPrincipal();
    Long tenantId  = p != null ? p.getTenantId() : null;
    Long userId    = p != null ? p.getUserId() : null;
    String username = p != null ? p.getUsername() : "system";
    auditLogMapper.insert(tenantId, userId, username, action, targetType, targetId, detail, clientIp());
  }

  /** 로그인처럼 SecurityContext 설정 전에 직접 사용자 정보를 지정하는 경우 */
  public void log(Long tenantId, Long userId, String username,
      String action, String targetType, String targetId, String detail) {
    auditLogMapper.insert(tenantId, userId, username, action, targetType, targetId, detail, clientIp());
  }

  public PageResponse<AuditLog> page(Long tenantIdOverride, String action, String targetType,
      int page, int size) {
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    Long tenantId = resolveForQuery(tenantIdOverride);
    long total = auditLogMapper.count(tenantId, action, targetType);
    List<AuditLog> items = auditLogMapper.findPage(tenantId, action, targetType, s, offset);
    return new PageResponse<>(items, p, s, total);
  }

  private Long resolveForQuery(Long override) {
    // SUPER_ADMIN은 override가 없으면 null(전체 조회), 있으면 해당 tenant
    if (tenantCtx.isSuperAdmin()) return override;
    return tenantCtx.currentTenantId();
  }

  private String clientIp() {
    try {
      var attrs = RequestContextHolder.getRequestAttributes();
      if (attrs instanceof ServletRequestAttributes sra) {
        HttpServletRequest req = sra.getRequest();
        String forwarded = req.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : req.getRemoteAddr();
      }
    } catch (Exception ignored) {}
    return null;
  }
}
