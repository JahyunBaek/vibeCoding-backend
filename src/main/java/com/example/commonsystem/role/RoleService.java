package com.example.commonsystem.role;

import com.example.commonsystem.common.PageResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

  private final RoleMapper roleMapper;

  public RoleService(RoleMapper roleMapper) {
    this.roleMapper = roleMapper;
  }

  public List<Role> all() {
    return roleMapper.findAll();
  }

  public PageResponse<Role> page(int page, int size) {
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    long total = roleMapper.count();
    List<Role> items = roleMapper.findPage(s, offset);
    return new PageResponse<>(items, p, s, total);
  }

  @Transactional
  public void create(RoleCreateCommand cmd) {
    roleMapper.insert(cmd);
  }

  @Transactional
  public void update(RoleUpdateCommand cmd) {
    roleMapper.update(cmd);
  }

  @Transactional
  public void delete(String roleKey) {
    roleMapper.delete(roleKey);
  }
}
