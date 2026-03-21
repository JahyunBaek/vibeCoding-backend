package com.example.commonsystem.common;

import com.example.commonsystem.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * SecurityContext에서 현재 요청의 테넌트 ID와 역할을 추출하는 유틸.
 * - SUPER_ADMIN: tenantId = null (모든 테넌트 데이터 접근 가능)
 * - ADMIN/USER:  tenantId = 해당 사용자의 테넌트 ID
 */
@Component
public class TenantContextHolder {

  /** 현재 사용자의 tenantId. SUPER_ADMIN이면 null. */
  public Long currentTenantId() {
    UserPrincipal p = currentPrincipal();
    return p == null ? null : p.getTenantId();
  }

  /**
   * SUPER_ADMIN이 override를 지정한 경우 그 값을 사용, 아니면 currentTenantId() 반환.
   * 일반 ADMIN/USER는 override를 무시하고 자신의 tenantId를 사용.
   */
  public Long resolveTenantId(Long override) {
    if (isSuperAdmin() && override != null) return override;
    return currentTenantId();
  }

  /** SUPER_ADMIN 여부 */
  public boolean isSuperAdmin() {
    UserPrincipal p = currentPrincipal();
    return p != null && p.isSuperAdmin();
  }

  public UserPrincipal currentPrincipal() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof UserPrincipal p) {
      return p;
    }
    return null;
  }
}
