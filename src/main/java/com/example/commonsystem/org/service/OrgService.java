package com.example.commonsystem.org.service;

import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.org.domain.Org;
import com.example.commonsystem.org.dto.OrgCreateCommand;
import com.example.commonsystem.org.dto.OrgNode;
import com.example.commonsystem.org.dto.OrgUpdateCommand;
import com.example.commonsystem.org.mapper.OrgMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrgService {

  private final OrgMapper orgMapper;

  public OrgService(OrgMapper orgMapper) {
    this.orgMapper = orgMapper;
  }

  public List<OrgNode> tree() {
    List<Org> orgs = orgMapper.findAll();
    Map<Long, OrgNode> map = new HashMap<>();
    for (Org o : orgs) map.put(o.orgId(), OrgNode.from(o));

    List<OrgNode> roots = new ArrayList<>();
    for (Org o : orgs) {
      OrgNode n = map.get(o.orgId());
      if (n.parentId == null) roots.add(n);
      else {
        OrgNode p = map.get(n.parentId);
        if (p != null) p.children.add(n);
        else roots.add(n);
      }
    }

    Comparator<OrgNode> cmp = Comparator.comparingInt((OrgNode n) -> n.sortOrder).thenComparingLong(n -> n.orgId);
    roots.sort(cmp);
    for (OrgNode r : roots) sortRec(r, cmp);
    return roots;
  }

  private void sortRec(OrgNode n, Comparator<OrgNode> cmp) {
    n.children.sort(cmp);
    for (OrgNode c : n.children) sortRec(c, cmp);
  }

  public PageResponse<Org> page(int page, int size) {
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    long total = orgMapper.count();
    List<Org> items = orgMapper.findPage(s, offset);
    return new PageResponse<>(items, p, s, total);
  }

  @Transactional
  public void create(OrgCreateCommand cmd) {
    orgMapper.insert(cmd);
  }

  @Transactional
  public void update(OrgUpdateCommand cmd) {
    orgMapper.update(cmd);
  }

  @Transactional
  public void delete(long orgId) {
    orgMapper.delete(orgId);
  }
}
