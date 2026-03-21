package com.example.commonsystem.menu.service;

import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.menu.domain.Menu;
import com.example.commonsystem.menu.dto.MenuCreateCommand;
import com.example.commonsystem.menu.dto.MenuNode;
import com.example.commonsystem.menu.dto.MenuUpdateCommand;
import com.example.commonsystem.menu.mapper.MenuMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {

  private final MenuMapper menuMapper;
  private final TenantContextHolder tenantCtx;

  public MenuService(MenuMapper menuMapper, TenantContextHolder tenantCtx) {
    this.menuMapper = menuMapper;
    this.tenantCtx  = tenantCtx;
  }

  public List<MenuNode> getMyMenuTree(String roleKey) {
    Long tenantId = tenantCtx.currentTenantId();
    List<Menu> menus = menuMapper.findByRole(roleKey, tenantId);
    return toTree(menus);
  }

  public List<MenuNode> getAllMenuTree(Long tenantIdOverride) {
    Long tenantId = tenantCtx.resolveTenantId(tenantIdOverride);
    return toTree(menuMapper.findAll(tenantId));
  }

  private List<MenuNode> toTree(List<Menu> menus) {
    Map<Long, MenuNode> map = new HashMap<>();
    for (Menu m : menus) {
      map.put(m.menuId(), MenuNode.from(m));
    }

    List<MenuNode> roots = new ArrayList<>();
    for (Menu m : menus) {
      MenuNode node = map.get(m.menuId());
      if (node.parentId == null) {
        roots.add(node);
      } else {
        MenuNode parent = map.get(node.parentId);
        if (parent != null) parent.children.add(node);
        else roots.add(node);
      }
    }

    Comparator<MenuNode> cmp = Comparator.comparingInt((MenuNode n) -> n.sortOrder)
        .thenComparingLong(n -> n.menuId);
    roots.sort(cmp);
    for (MenuNode r : roots) sortRec(r, cmp);
    return roots;
  }

  private void sortRec(MenuNode node, Comparator<MenuNode> cmp) {
    node.children.sort(cmp);
    for (MenuNode c : node.children) sortRec(c, cmp);
  }

  @Transactional
  public long create(MenuCreateCommand cmd, List<String> roleKeys) {
    if (cmd.getTenantId() == null) {
      cmd.setTenantId(tenantCtx.currentTenantId());
    }
    menuMapper.insert(cmd);
    long menuId = cmd.getMenuId();
    if (roleKeys != null) {
      for (String rk : filterRoleKeys(roleKeys)) {
        menuMapper.insertRole(menuId, rk);
      }
    }
    return menuId;
  }

  @Transactional
  public void update(MenuUpdateCommand cmd) {
    menuMapper.update(cmd);
  }

  @Transactional
  public void delete(long menuId) {
    menuMapper.delete(menuId);
  }

  @Transactional
  public void setRoles(long menuId, List<String> roleKeys) {
    menuMapper.deleteRoles(menuId);
    if (roleKeys != null) {
      for (String rk : filterRoleKeys(roleKeys)) {
        menuMapper.insertRole(menuId, rk);
      }
    }
  }

  /** SUPER_ADMIN이 아닌 경우 SUPER_ADMIN 역할 키 제거 */
  private List<String> filterRoleKeys(List<String> roleKeys) {
    if (tenantCtx.isSuperAdmin()) return roleKeys;
    return roleKeys.stream().filter(k -> !"SUPER_ADMIN".equals(k)).toList();
  }
}
