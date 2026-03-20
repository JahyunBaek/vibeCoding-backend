package com.example.commonsystem.org.dto;

import com.example.commonsystem.org.domain.Org;
import java.util.ArrayList;
import java.util.List;

public class OrgNode {
  public long orgId;
  public Long parentId;
  public String name;
  public int sortOrder;
  public boolean useYn;
  public List<OrgNode> children = new ArrayList<>();

  public static OrgNode from(Org o) {
    OrgNode n = new OrgNode();
    n.orgId = o.orgId();
    n.parentId = o.parentId();
    n.name = o.name();
    n.sortOrder = o.sortOrder();
    n.useYn = o.useYn();
    return n;
  }
}
