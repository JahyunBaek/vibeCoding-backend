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
  private final boolean enabled;

  public UserPrincipal(long userId, String username, String passwordHash, String roleKey, String name, Long orgId, boolean enabled) {
    this.userId = userId;
    this.username = username;
    this.passwordHash = passwordHash;
    this.roleKey = roleKey;
    this.name = name;
    this.orgId = orgId;
    this.enabled = enabled;
  }

  public long getUserId() { return userId; }
  public String getRoleKey() { return roleKey; }
  public String getName() { return name; }
  public Long getOrgId() { return orgId; }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + roleKey));
  }

  @Override
  public String getPassword() { return passwordHash; }

  @Override
  public String getUsername() { return username; }

  @Override
  public boolean isAccountNonExpired() { return true; }

  @Override
  public boolean isAccountNonLocked() { return true; }

  @Override
  public boolean isCredentialsNonExpired() { return true; }

  @Override
  public boolean isEnabled() { return enabled; }
}
