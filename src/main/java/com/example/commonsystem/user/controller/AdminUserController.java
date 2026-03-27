package com.example.commonsystem.user.controller;

import com.example.commonsystem.auth.dto.AuthDtos.ResetTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.commonsystem.auth.service.PasswordResetService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.CsvExportService;
import com.example.commonsystem.common.EmailService;
import com.example.commonsystem.common.PageResponse;
import com.example.commonsystem.common.I18nService;
import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.tenant.service.TenantConfigService;
import com.example.commonsystem.user.dto.UserListRow;
import com.example.commonsystem.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 - 사용자", description = "사용자 관리")
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminUserController {

  private final UserService userService;
  private final PasswordResetService passwordResetService;
  private final CsvExportService csvExportService;
  private final EmailService emailService;
  private final I18nService i18n;
  private final TenantContextHolder tenantCtx;
  private final TenantConfigService tenantConfigService;

  public AdminUserController(UserService userService, PasswordResetService passwordResetService,
      CsvExportService csvExportService, EmailService emailService, I18nService i18n,
      TenantContextHolder tenantCtx, TenantConfigService tenantConfigService) {
    this.userService = userService;
    this.passwordResetService = passwordResetService;
    this.csvExportService = csvExportService;
    this.emailService = emailService;
    this.i18n = i18n;
    this.tenantCtx = tenantCtx;
    this.tenantConfigService = tenantConfigService;
  }

  @Operation(summary = "사용자 목록 조회")
  @GetMapping
  public ApiResponse<PageResponse<UserListRow>> list(
      @RequestParam(required = false) Long orgId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) Long tenantId
  ) {
    return ApiResponse.ok(userService.page(orgId, page, size, tenantId));
  }

  @Operation(summary = "사용자 목록 CSV 내보내기")
  @GetMapping("/export")
  public ResponseEntity<byte[]> export(@RequestParam(required = false) Long tenantId) {
    List<UserListRow> users = userService.listAll(tenantId);
    String locale = tenantConfigService.getLocale(tenantCtx.currentTenantId());
    List<String> headers = List.of(
        i18n.getMessage("csv.users.userId", locale),
        i18n.getMessage("csv.users.username", locale),
        i18n.getMessage("csv.users.name", locale),
        i18n.getMessage("csv.users.role", locale),
        i18n.getMessage("csv.users.org", locale),
        i18n.getMessage("csv.users.active", locale)
    );
    List<List<String>> rows = new ArrayList<>();
    for (UserListRow u : users) {
      rows.add(List.of(
          String.valueOf(u.userId()),
          u.username(),
          u.name() != null ? u.name() : "",
          u.roleKey() != null ? u.roleKey() : "",
          u.orgId() != null ? String.valueOf(u.orgId()) : "",
          u.enabled() ? "Y" : "N"
      ));
    }
    String csv = csvExportService.toCsv(headers, rows);
    byte[] body = csv.getBytes(StandardCharsets.UTF_8);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .body(body);
  }

  public record CreateUserRequest(
      @NotBlank @Size(min = 3, max = 50) String username,
      @NotBlank @Size(min = 8, max = 100) String password,
      @NotBlank @Size(max = 50) String name,
      @NotBlank String roleKey,
      Long orgId,
      Boolean enabled,
      Long tenantId
  ) {}

  @Operation(summary = "사용자 생성")
  @PostMapping
  public ApiResponse<Void> create(@Valid @RequestBody CreateUserRequest req) {
    userService.create(req.username(), req.password(), req.name(), req.roleKey(), req.orgId(), req.enabled() == null || req.enabled(), req.tenantId());
    return ApiResponse.ok();
  }

  public record UpdateUserRequest(
      @Size(min = 8, max = 100) String password,
      @NotBlank @Size(max = 50) String name,
      @NotBlank String roleKey,
      Long orgId,
      Boolean enabled
  ) {}

  @Operation(summary = "사용자 수정")
  @PutMapping("/{userId}")
  public ApiResponse<Void> update(@PathVariable long userId, @Valid @RequestBody UpdateUserRequest req) {
    userService.update(userId, req.name(), req.password(), req.roleKey(), req.orgId(), req.enabled() == null || req.enabled());
    return ApiResponse.ok();
  }

  public record ResetPasswordRequest(@NotBlank @Size(min = 8, max = 100) String newPassword) {}

  @Operation(summary = "사용자 비밀번호 초기화")
  @PatchMapping("/{userId}/password")
  public ApiResponse<Void> resetPassword(@PathVariable long userId, @Valid @RequestBody ResetPasswordRequest req) {
    userService.adminResetPassword(userId, req.newPassword());
    return ApiResponse.ok();
  }

  @Operation(summary = "비밀번호 재설정 토큰 생성")
  @PostMapping("/{userId}/reset-token")
  public ApiResponse<ResetTokenResponse> generateResetToken(@PathVariable long userId) {
    String token = passwordResetService.generateResetToken(userId);
    // 사용자 정보를 조회하여 이메일 발송 시도 (email 컬럼 추가 전까지 username을 수신 주소로 사용)
    var user = userService.me(userId);
    if (user != null) {
      String locale = tenantConfigService.getLocale(tenantCtx.currentTenantId());
      emailService.sendPasswordReset(user.username(), user.name(), token, locale);
    }
    return ApiResponse.ok(new ResetTokenResponse(token, passwordResetService.getExpiresInMinutes()));
  }

  @Operation(summary = "사용자 삭제")
  @DeleteMapping("/{userId}")
  public ApiResponse<Void> delete(@PathVariable long userId) {
    userService.delete(userId);
    return ApiResponse.ok();
  }
}
