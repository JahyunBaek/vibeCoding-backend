package com.example.commonsystem.user;

import com.example.commonsystem.common.PageResponse;
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
  public void updateMe(long userId, String name, String newPassword) {
    User current = userMapper.findById(userId);
    if (current == null) {
      return;
    }
    String hash = (newPassword == null || newPassword.isBlank()) ? null : passwordEncoder.encode(newPassword);
    userMapper.update(new UserUpdateCommand(
        userId,
        hash,
        name,
        null,  // keep role
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
