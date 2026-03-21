package com.example.commonsystem.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

  private final long userId;
  private final String username;
  private final String passwordHash;
  private final String roleKey;
  private final String name;
  private final Long orgId;
  private final Long tenantId; // null = SUPER_ADMIN (시스템 레벨)
  private final boolean enabled;

  public UserPrincipal(long userId, String username, String passwordHash,
      String roleKey, String name, Long orgId, Long tenantId, boolean enabled) {
    this.userId = userId;
    this.username = username;
    this.passwordHash = passwordHash;
    this.roleKey = roleKey;
    this.name = name;
    this.orgId = orgId;
    this.tenantId = tenantId;
    this.enabled = enabled;
  }

  public long getUserId()     { return userId; }
  public String getRoleKey()  { return roleKey; }
  public String getName()     { return name; }
  public Long getOrgId()      { return orgId; }
  public Long getTenantId()   { return tenantId; }

  /** SUPER_ADMIN은 tenantId == null */
  public boolean isSuperAdmin() { return "SUPER_ADMIN".equals(roleKey); }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + roleKey));
  }

  @Override public String getPassword()              { return passwordHash; }
  @Override public String getUsername()              { return username; }
  @Override public boolean isAccountNonExpired()     { return true; }
  @Override public boolean isAccountNonLocked()      { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled()               { return enabled; }
}
