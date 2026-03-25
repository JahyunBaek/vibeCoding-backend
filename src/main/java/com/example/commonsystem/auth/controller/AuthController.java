package com.example.commonsystem.auth.controller;

import com.example.commonsystem.audit.service.AuditService;
import com.example.commonsystem.auth.dto.AuthDtos.LoginRequest;
import com.example.commonsystem.auth.dto.AuthDtos.ResetPasswordRequest;
import com.example.commonsystem.auth.dto.AuthDtos.TokenResponse;
import com.example.commonsystem.auth.dto.AuthDtos.UserSummary;
import com.example.commonsystem.auth.service.PasswordResetService;
import com.example.commonsystem.auth.service.RefreshTokenService;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.RateLimitService;
import com.example.commonsystem.security.JwtProperties;
import com.example.commonsystem.security.JwtService;
import com.example.commonsystem.security.SecurityProperties;
import com.example.commonsystem.security.UserPrincipal;
import com.example.commonsystem.user.domain.User;
import com.example.commonsystem.user.mapper.UserMapper;
import com.example.commonsystem.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "로그인, 토큰 갱신, 로그아웃, 비밀번호 재설정")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final SecurityProperties securityProperties;
  private final PasswordResetService passwordResetService;
  private final RefreshTokenService refreshTokenService;
  private final UserMapper userMapper;
  private final UserService userService;
  private final AuditService auditService;
  private final RateLimitService rateLimitService;

  @Operation(summary = "로그인")
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokenResponse>> login(
      @Valid @RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse res) {
    String clientIp = extractClientIp(request);
    if (!rateLimitService.tryAcquire("login:" + clientIp, 10, Duration.ofMinutes(5))) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body(ApiResponse.fail(ErrorCode.VALIDATION, "로그인 시도가 너무 많습니다. 5분 후 다시 시도해주세요."));
    }

    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.username(), req.password())
    );
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    String access = jwtService.createAccessToken(
        principal.getUserId(),
        principal.getUsername(),
        principal.getRoleKey(),
        principal.getName(),
        principal.getOrgId(),
        principal.getTenantId()
    );

    Duration refreshTtl = Duration.ofMinutes(jwtProperties.refreshTokenMinutes());
    String refresh = refreshTokenService.issue(principal.getUserId(), refreshTtl);
    setRefreshCookie(res, refresh, refreshTtl);

    TokenResponse body = new TokenResponse(access, new UserSummary(
        principal.getUserId(), principal.getUsername(), principal.getName(),
        principal.getRoleKey(), principal.getOrgId(), principal.getTenantId()
    ));
    auditService.log(principal.getTenantId(), principal.getUserId(), principal.getUsername(),
        "LOGIN", "USER", String.valueOf(principal.getUserId()), null);
    return ResponseEntity.ok(ApiResponse.ok(body));
  }

  @Operation(summary = "토큰 갱신")
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenResponse>> refresh(
      @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken,
      HttpServletResponse res
  ) {
    Long userId = refreshTokenService.verifyAndGetUserId(refreshToken);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));
    }

    User user = userMapper.findById(userId);
    if (user == null || !user.enabled()) {
      refreshTokenService.revoke(refreshToken);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED, "User not found"));
    }

    // Rotation: revoke old -> issue new
    refreshTokenService.revoke(refreshToken);

    Duration refreshTtl = Duration.ofMinutes(jwtProperties.refreshTokenMinutes());
    String newRefresh = refreshTokenService.issue(user.userId(), refreshTtl);
    setRefreshCookie(res, newRefresh, refreshTtl);

    String access = jwtService.createAccessToken(
        user.userId(),
        user.username(),
        user.roleKey(),
        user.name(),
        user.orgId(),
        user.tenantId()
    );

    TokenResponse body = new TokenResponse(access, new UserSummary(
        user.userId(), user.username(), user.name(), user.roleKey(),
        user.orgId(), user.tenantId()
    ));
    return ResponseEntity.ok(ApiResponse.ok(body));
  }

  @Operation(summary = "로그아웃")
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken,
      HttpServletResponse res,
      @AuthenticationPrincipal UserPrincipal principal
  ) {
    if (principal != null) {
      auditService.log(principal.getTenantId(), principal.getUserId(), principal.getUsername(),
          "LOGOUT", "USER", String.valueOf(principal.getUserId()), null);
    }
    refreshTokenService.revoke(refreshToken);
    clearRefreshCookie(res);
    return ResponseEntity.ok(ApiResponse.ok());
  }

  @Operation(summary = "비밀번호 재설정")
  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest req
  ) {
    Long userId = passwordResetService.validateAndGetUserId(req.token());
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.fail(ErrorCode.VALIDATION, "유효하지 않거나 만료된 재설정 토큰입니다."));
    }
    userService.resetPasswordByToken(userId, req.newPassword());
    return ResponseEntity.ok(ApiResponse.ok());
  }

  private void setRefreshCookie(HttpServletResponse res, String token, Duration ttl) {
    ResponseCookie cookie = ResponseCookie.from(securityProperties.refreshCookieName(), token)
        .httpOnly(true)
        .secure(securityProperties.refreshCookieSecure())
        .path(securityProperties.refreshCookiePath())
        .sameSite(securityProperties.refreshCookieSamesite())
        .maxAge(ttl)
        .build();
    res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private String extractClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private void clearRefreshCookie(HttpServletResponse res) {
    ResponseCookie cookie = ResponseCookie.from(securityProperties.refreshCookieName(), "")
        .httpOnly(true)
        .secure(securityProperties.refreshCookieSecure())
        .path(securityProperties.refreshCookiePath())
        .sameSite(securityProperties.refreshCookieSamesite())
        .maxAge(Duration.ZERO)
        .build();
    res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
