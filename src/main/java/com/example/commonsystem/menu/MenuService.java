package com.example.commonsystem.menu;

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

  public MenuService(MenuMapper menuMapper) {
    this.menuMapper = menuMapper;
  }

  public List<MenuNode> getMyMenuTree(String roleKey) {
    List<Menu> menus = menuMapper.findByRole(roleKey);
    return toTree(menus);
  }

  public List<MenuNode> getAllMenuTree() {
    return toTree(menuMapper.findAll());
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

    Comparator<MenuNode> cmp = Comparator.comparingInt((MenuNode n) -> n.sortOrder).thenComparingLong(n -> n.menuId);
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
    menuMapper.insert(cmd);
    long menuId = cmd.getMenuId();
    if (roleKeys != null) {
      for (String rk : roleKeys) {
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
      for (String rk : roleKeys) {
        menuMapper.insertRole(menuId, rk);
      }
    }
  }
}
