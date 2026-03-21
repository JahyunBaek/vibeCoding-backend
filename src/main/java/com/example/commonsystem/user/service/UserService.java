package com.example.commonsystem.user.service;

import com.example.commonsystem.audit.service.AuditService;
import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.user.domain.User;
import com.example.commonsystem.user.dto.UserCreateCommand;
import com.example.commonsystem.user.dto.UserListRow;
import com.example.commonsystem.user.dto.UserUpdateCommand;
import com.example.commonsystem.user.mapper.UserMapper;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final TenantContextHolder tenantCtx;
  private final AuditService auditService;

  public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder,
      TenantContextHolder tenantCtx, AuditService auditService) {
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.tenantCtx = tenantCtx;
    this.auditService = auditService;
  }

  public User me(long userId) {
    return userMapper.findById(userId);
  }

  @Transactional
  public void updateMe(long userId, String name, String currentPassword, String newPassword) {
    User current = userMapper.findById(userId);
    if (current == null) return;

    String hash = null;
    if (newPassword != null && !newPassword.isBlank()) {
      if (currentPassword == null || currentPassword.isBlank()) {
        throw new AppException(ErrorCode.VALIDATION, "현재 비밀번호를 입력해주세요.");
      }
      if (!passwordEncoder.matches(currentPassword, current.passwordHash())) {
        throw new AppException(ErrorCode.VALIDATION, "현재 비밀번호가 올바르지 않습니다.");
      }
      if (newPassword.length() < 8) {
        throw new AppException(ErrorCode.VALIDATION, "새 비밀번호는 8자 이상이어야 합니다.");
      }
      hash = passwordEncoder.encode(newPassword);
    }

    userMapper.update(new UserUpdateCommand(userId, hash, name, null, current.orgId(), current.enabled()));
  }

  public PageResponse<UserListRow> page(Long orgId, int page, int size, Long tenantIdOverride) {
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    Long tenantId = tenantCtx.resolveTenantId(tenantIdOverride);
    long total = userMapper.count(tenantId, orgId);
    List<UserListRow> items = userMapper.findPage(tenantId, orgId, s, offset);
    return new PageResponse<>(items, p, s, total);
  }

  @Transactional
  public void create(String username, String password, String name, String roleKey,
      Long orgId, boolean enabled, Long tenantIdOverride) {
    if ("SUPER_ADMIN".equals(roleKey) && !tenantCtx.isSuperAdmin()) {
      throw new AppException(ErrorCode.FORBIDDEN, "SUPER_ADMIN 역할은 슈퍼 관리자만 부여할 수 있습니다.");
    }
    Long tenantId = tenantCtx.resolveTenantId(tenantIdOverride);
    String hash = passwordEncoder.encode(password);
    userMapper.insert(new UserCreateCommand(username, hash, name, roleKey, orgId, tenantId, enabled));
    auditService.log("CREATE", "USER", username, "name=" + name + ", role=" + roleKey);
  }

  @Transactional
  public void update(long userId, String name, String password, String roleKey,
      Long orgId, boolean enabled) {
    if ("SUPER_ADMIN".equals(roleKey) && !tenantCtx.isSuperAdmin()) {
      throw new AppException(ErrorCode.FORBIDDEN, "SUPER_ADMIN 역할은 슈퍼 관리자만 부여할 수 있습니다.");
    }
    User target = userMapper.findById(userId);
    if (target != null && "SUPER_ADMIN".equals(target.roleKey()) && !tenantCtx.isSuperAdmin()) {
      throw new AppException(ErrorCode.FORBIDDEN, "SUPER_ADMIN 계정은 슈퍼 관리자만 수정할 수 있습니다.");
    }
    String hash = (password == null || password.isBlank()) ? null : passwordEncoder.encode(password);
    userMapper.update(new UserUpdateCommand(userId, hash, name, roleKey, orgId, enabled));
  }

  @Transactional
  public void adminResetPassword(long userId, String newPassword) {
    User target = userMapper.findById(userId);
    if (target == null) throw new AppException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다.");
    if ("SUPER_ADMIN".equals(target.roleKey()) && !tenantCtx.isSuperAdmin()) {
      throw new AppException(ErrorCode.FORBIDDEN, "SUPER_ADMIN 계정은 슈퍼 관리자만 수정할 수 있습니다.");
    }
    if (newPassword == null || newPassword.length() < 8) {
      throw new AppException(ErrorCode.VALIDATION, "새 비밀번호는 8자 이상이어야 합니다.");
    }
    String hash = passwordEncoder.encode(newPassword);
    userMapper.update(new UserUpdateCommand(userId, hash, target.name(), target.roleKey(), target.orgId(), target.enabled()));
  }

  @Transactional
  public void delete(long userId) {
    User target = userMapper.findById(userId);
    if (target != null && "SUPER_ADMIN".equals(target.roleKey()) && !tenantCtx.isSuperAdmin()) {
      throw new AppException(ErrorCode.FORBIDDEN, "SUPER_ADMIN 계정은 슈퍼 관리자만 삭제할 수 있습니다.");
    }
    if (target != null) {
      auditService.log("DELETE", "USER", target.username(), "userId=" + userId);
    }
    userMapper.delete(userId);
  }
}
