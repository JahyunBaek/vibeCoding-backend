package com.example.commonsystem.user;

import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.exception.AppException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
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
      // 현재 비밀번호 필수 확인
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

    userMapper.update(new UserUpdateCommand(
        userId,
        hash,
        name,
        null,           // keep role
        current.orgId(),
        current.enabled()
    ));
  }

  public PageResponse<UserListRow> page(Long orgId, int page, int size) {
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    long total = userMapper.count(orgId);
    List<UserListRow> items = userMapper.findPage(orgId, s, offset);
    return new PageResponse<>(items, p, s, total);
  }

  @Transactional
  public void create(String username, String password, String name, String roleKey, Long orgId, boolean enabled) {
    String hash = passwordEncoder.encode(password);
    userMapper.insert(new UserCreateCommand(username, hash, name, roleKey, orgId, enabled));
  }

  @Transactional
  public void update(long userId, String name, String password, String roleKey, Long orgId, boolean enabled) {
    String hash = (password == null || password.isBlank()) ? null : passwordEncoder.encode(password);
    userMapper.update(new UserUpdateCommand(userId, hash, name, roleKey, orgId, enabled));
  }

  @Transactional
  public void delete(long userId) {
    userMapper.delete(userId);
  }
}
