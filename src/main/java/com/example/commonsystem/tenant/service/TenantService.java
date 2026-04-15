package com.example.commonsystem.tenant.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Medical sample menus (between Dashboard and Boards)
    long mMedical   = tenantMapper.getMenuIdSeqNext();
    long mPatients  = tenantMapper.getMenuIdSeqNext();
    long mTrials    = tenantMapper.getMenuIdSeqNext();
    tenantMapper.insertMenu(tid, mMedical,  null,     "Medical",         null,               "heart-pulse",    5,  "GROUP", null);
    tenantMapper.insertMenu(tid, mPatients, mMedical, "Patients",        "/sample/patients",  "users",          0,  "MENU",  null);
    tenantMapper.insertMenu(tid, mTrials,   mMedical, "Clinical Trials", "/sample/trials",    "flask-conical",  10, "MENU",  null);

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
    long[] allMenus = { mDashboard, mMedical, mPatients, mTrials, mBoards, mAdmin, mCodes, mBoardsAdmin, mUsers, mOrgs, mMenus, mRoles, mScreens, mSettings, mAudit, mMyInfo };
    for (long menuId : allMenus) {
      tenantMapper.insertMenuRole(menuId, "ADMIN");
    }
    // USER: Dashboard, Medical, Boards group, My Info
    tenantMapper.insertMenuRole(mDashboard, "USER");
    tenantMapper.insertMenuRole(mMedical,   "USER");
    tenantMapper.insertMenuRole(mPatients,  "USER");
    tenantMapper.insertMenuRole(mTrials,    "USER");
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

    // Medical sample common codes
    tenantMapper.insertCodeGroup(tid, "PATIENT_STATUS", "환자 상태");
    tenantMapper.insertCode(tid, "PATIENT_STATUS", "ACTIVE", "활성", "ACTIVE", 0);
    tenantMapper.insertCode(tid, "PATIENT_STATUS", "DISCHARGED", "퇴원", "DISCHARGED", 1);
    tenantMapper.insertCode(tid, "PATIENT_STATUS", "FOLLOW_UP", "추적관찰", "FOLLOW_UP", 2);
    tenantMapper.insertCode(tid, "PATIENT_STATUS", "INACTIVE", "비활성", "INACTIVE", 3);

    tenantMapper.insertCodeGroup(tid, "DEPARTMENT", "진료과");
    tenantMapper.insertCode(tid, "DEPARTMENT", "IM", "내과", "IM", 0);
    tenantMapper.insertCode(tid, "DEPARTMENT", "GS", "외과", "GS", 1);
    tenantMapper.insertCode(tid, "DEPARTMENT", "NR", "신경과", "NR", 2);
    tenantMapper.insertCode(tid, "DEPARTMENT", "CD", "심장내과", "CD", 3);
    tenantMapper.insertCode(tid, "DEPARTMENT", "OG", "산부인과", "OG", 4);
    tenantMapper.insertCode(tid, "DEPARTMENT", "PD", "소아과", "PD", 5);
    tenantMapper.insertCode(tid, "DEPARTMENT", "OS", "정형외과", "OS", 6);
    tenantMapper.insertCode(tid, "DEPARTMENT", "DR", "피부과", "DR", 7);

    tenantMapper.insertCodeGroup(tid, "BLOOD_TYPE", "혈액형");
    tenantMapper.insertCode(tid, "BLOOD_TYPE", "A_POS", "A+", "A+", 0);
    tenantMapper.insertCode(tid, "BLOOD_TYPE", "A_NEG", "A-", "A-", 1);
    tenantMapper.insertCode(tid, "BLOOD_TYPE", "B_POS", "B+", "B+", 2);
    tenantMapper.insertCode(tid, "BLOOD_TYPE", "B_NEG", "B-", "B-", 3);
    tenantMapper.insertCode(tid, "BLOOD_TYPE", "O_POS", "O+", "O+", 4);
    tenantMapper.insertCode(tid, "BLOOD_TYPE", "O_NEG", "O-", "O-", 5);
    tenantMapper.insertCode(tid, "BLOOD_TYPE", "AB_POS", "AB+", "AB+", 6);
    tenantMapper.insertCode(tid, "BLOOD_TYPE", "AB_NEG", "AB-", "AB-", 7);

    tenantMapper.insertCodeGroup(tid, "GENDER", "성별");
    tenantMapper.insertCode(tid, "GENDER", "M", "남성", "M", 0);
    tenantMapper.insertCode(tid, "GENDER", "F", "여성", "F", 1);

    tenantMapper.insertCodeGroup(tid, "TRIAL_PHASE", "임상시험 단계");
    tenantMapper.insertCode(tid, "TRIAL_PHASE", "PHASE_1", "Phase I", "PHASE_1", 0);
    tenantMapper.insertCode(tid, "TRIAL_PHASE", "PHASE_2", "Phase II", "PHASE_2", 1);
    tenantMapper.insertCode(tid, "TRIAL_PHASE", "PHASE_3", "Phase III", "PHASE_3", 2);
    tenantMapper.insertCode(tid, "TRIAL_PHASE", "PHASE_4", "Phase IV", "PHASE_4", 3);

    tenantMapper.insertCodeGroup(tid, "TRIAL_STATUS", "임상시험 상태");
    tenantMapper.insertCode(tid, "TRIAL_STATUS", "PLANNED", "계획", "PLANNED", 0);
    tenantMapper.insertCode(tid, "TRIAL_STATUS", "RECRUITING", "모집중", "RECRUITING", 1);
    tenantMapper.insertCode(tid, "TRIAL_STATUS", "ACTIVE", "진행중", "ACTIVE", 2);
    tenantMapper.insertCode(tid, "TRIAL_STATUS", "COMPLETED", "완료", "COMPLETED", 3);
    tenantMapper.insertCode(tid, "TRIAL_STATUS", "SUSPENDED", "중단", "SUSPENDED", 4);

    // 6. 초기 관리자 계정 생성
    String hash = passwordEncoder.encode(adminPassword);
    tenantMapper.insertUser(tid, adminUsername, hash, adminUsername, "ADMIN");

    // 7. 테넌트 기본 설정 초기화
    configService.initDefaults(tid, tenantName);
  }
}
