package com.example.commonsystem.tenant.service;

import com.example.commonsystem.audit.service.AuditService;
import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.tenant.domain.Tenant;
import com.example.commonsystem.tenant.dto.TenantCreateCommand;
import com.example.commonsystem.tenant.dto.TenantCreateResult;
import com.example.commonsystem.tenant.dto.TenantListRow;
import com.example.commonsystem.tenant.dto.TenantUpdateCommand;
import com.example.commonsystem.tenant.mapper.TenantMapper;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

  private final TenantMapper tenantMapper;
  private final PasswordEncoder passwordEncoder;
  private final TenantConfigService configService;
  private final AuditService auditService;

  public TenantService(TenantMapper tenantMapper, PasswordEncoder passwordEncoder,
      TenantConfigService configService, AuditService auditService) {
    this.tenantMapper = tenantMapper;
    this.passwordEncoder = passwordEncoder;
    this.configService = configService;
    this.auditService = auditService;
  }

  public PageResponse<TenantListRow> page(int page, int size) {
    int p = Math.max(page, 1);
    int s = Math.min(Math.max(size, 1), 100);
    int offset = (p - 1) * s;
    long total = tenantMapper.count();
    List<TenantListRow> items = tenantMapper.findPage(s, offset);
    return new PageResponse<>(items, p, s, total);
  }

  public List<TenantListRow> all() {
    return tenantMapper.findAll();
  }

  public Tenant findById(long tenantId) {
    Tenant t = tenantMapper.findById(tenantId);
    if (t == null) throw new AppException(ErrorCode.NOT_FOUND, "Tenant not found");
    return t;
  }

  /**
   * 테넌트 생성 + 초기 데이터(메뉴, 게시판, 역할 권한, 공통코드, 관리자 계정) 자동 provisioning.
   */
  @Transactional
  public TenantCreateResult create(String tenantKey, String tenantName, String planType,
      String adminUsername, String adminPassword) {
    TenantCreateCommand cmd = new TenantCreateCommand(tenantKey, tenantName, planType, true);
    tenantMapper.insert(cmd);
    long tid = cmd.getTenantId();

    provisionTenant(tid, tenantName, adminUsername, adminPassword);
    auditService.log("CREATE", "TENANT", String.valueOf(tid), "key=" + tenantKey + ", name=" + tenantName);
    return new TenantCreateResult(tid, adminUsername, adminPassword);
  }

  @Transactional
  public void update(long tenantId, String tenantName, String planType, boolean active) {
    tenantMapper.update(new TenantUpdateCommand(tenantId, tenantName, planType, active));
  }

  @Transactional
  public void delete(long tenantId) {
    auditService.log("DELETE", "TENANT", String.valueOf(tenantId), null);
    tenantMapper.delete(tenantId);
  }

  // -------------------------------------------------------
  // 신규 테넌트 초기 데이터 생성
  // -------------------------------------------------------
  private void provisionTenant(long tid, String tenantName, String adminUsername, String adminPassword) {
    // 1. 기본 역할 권한 부여 (ADMIN에게 모든 action)
    tenantMapper.insertRoleActions(tid, "ADMIN");

    // 2. 기본 메뉴 트리 생성
    long mDashboard = tenantMapper.getMenuIdSeqNext();
    long mBoards    = tenantMapper.getMenuIdSeqNext();
    long mAdmin     = tenantMapper.getMenuIdSeqNext();
    long mCodes     = tenantMapper.getMenuIdSeqNext();
    long mBoardsAdmin = tenantMapper.getMenuIdSeqNext();
    long mUsers     = tenantMapper.getMenuIdSeqNext();
    long mOrgs      = tenantMapper.getMenuIdSeqNext();
    long mMenus     = tenantMapper.getMenuIdSeqNext();
    long mRoles     = tenantMapper.getMenuIdSeqNext();
    long mScreens   = tenantMapper.getMenuIdSeqNext();
    long mMyInfo    = tenantMapper.getMenuIdSeqNext();

    tenantMapper.insertMenu(tid, mDashboard, null, "Dashboard",   "/dashboard",     "layout-dashboard", 0,  "MENU", null);
    tenantMapper.insertMenu(tid, mBoards,    null, "Boards",       null,             "file-text",        10, "GROUP", null);
    tenantMapper.insertMenu(tid, mAdmin,     null, "Admin",        null,             "settings",         20, "GROUP", null);
    tenantMapper.insertMenu(tid, mCodes,     mAdmin, "Common Codes", "/admin/codes", "code",             0,  "MENU", null);
    tenantMapper.insertMenu(tid, mBoardsAdmin, mAdmin, "Boards",  "/admin/boards",  "clipboard",        10, "MENU", null);
    tenantMapper.insertMenu(tid, mUsers,     mAdmin, "Users",     "/admin/users",   "users",            20, "MENU", null);
    tenantMapper.insertMenu(tid, mOrgs,      mAdmin, "Orgs",      "/admin/orgs",    "building",         30, "MENU", null);
    tenantMapper.insertMenu(tid, mMenus,     mAdmin, "Menus",     "/admin/menus",   "menu",             40, "MENU", null);
    tenantMapper.insertMenu(tid, mRoles,     mAdmin, "Roles",     "/admin/roles",   "shield",           50, "MENU", null);
    tenantMapper.insertMenu(tid, mScreens,   mAdmin, "ScreenActions", "/admin/screens", "lock",         60, "MENU", null);
    long mSettings  = tenantMapper.getMenuIdSeqNext();
    long mAudit     = tenantMapper.getMenuIdSeqNext();
    tenantMapper.insertMenu(tid, mSettings,  mAdmin, "Settings",    "/admin/settings","sliders",         70, "MENU", null);
    tenantMapper.insertMenu(tid, mAudit,     mAdmin, "Audit Log",   "/admin/audit",   "history",         80, "MENU", null);
    tenantMapper.insertMenu(tid, mMyInfo,    null, "My Info",     "/me",            "user",             30, "MENU", null);

    // 3. ADMIN: 모든 메뉴 접근
    long[] allMenus = { mDashboard, mBoards, mAdmin, mCodes, mBoardsAdmin, mUsers, mOrgs, mMenus, mRoles, mScreens, mSettings, mAudit, mMyInfo };
    for (long menuId : allMenus) {
      tenantMapper.insertMenuRole(menuId, "ADMIN");
    }
    // USER: Dashboard, Boards group, My Info
    tenantMapper.insertMenuRole(mDashboard, "USER");
    tenantMapper.insertMenuRole(mBoards,    "USER");
    tenantMapper.insertMenuRole(mMyInfo,    "USER");

    // 4. 기본 게시판 "공지사항" 생성
    long boardId = tenantMapper.getBoardIdSeqNext();
    tenantMapper.insertBoard(tid, boardId, "공지사항", "기본 게시판");

    long mNotice = tenantMapper.getMenuIdSeqNext();
    tenantMapper.insertMenu(tid, mNotice, mBoards, "공지사항",
        "/boards/" + boardId, "clipboard-list", 0, "BOARD", boardId);
    tenantMapper.insertMenuRole(mNotice, "ADMIN");
    tenantMapper.insertMenuRole(mNotice, "USER");

    // 5. 공통 코드 기본값
    tenantMapper.insertCodeGroup(tid, "YN", "Yes/No");
    tenantMapper.insertCode(tid, "YN", "Y", "Yes", "Y", 0);
    tenantMapper.insertCode(tid, "YN", "N", "No",  "N", 10);

    // 6. 초기 관리자 계정 생성
    String hash = passwordEncoder.encode(adminPassword);
    tenantMapper.insertUser(tid, adminUsername, hash, adminUsername, "ADMIN");

    // 7. 테넌트 기본 설정 초기화
    configService.initDefaults(tid, tenantName);
  }
}
