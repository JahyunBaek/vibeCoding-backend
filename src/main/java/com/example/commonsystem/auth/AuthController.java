package com.example.commonsystem.auth;

import com.example.commonsystem.auth.AuthDtos.LoginRequest;
import com.example.commonsystem.auth.AuthDtos.TokenResponse;
import com.example.commonsystem.auth.AuthDtos.UserSummary;
import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.security.JwtProperties;
import com.example.commonsystem.security.JwtService;
import com.example.commonsystem.security.SecurityProperties;
import com.example.commonsystem.security.UserPrincipal;
import com.example.commonsystem.user.User;
import com.example.commonsystem.user.UserMapper;
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
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final SecurityProperties securityProperties;
  private final RefreshTokenService refreshTokenService;
  private final UserMapper userMapper;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest req, HttpServletResponse res) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.username(), req.password())
    );
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    String access = jwtService.createAccessToken(
        principal.getUserId(),
        principal.getUsername(),
        principal.getRoleKey(),
        principal.getName(),
        principal.getOrgId()
    );

    Duration refreshTtl = Duration.ofMinutes(jwtProperties.refreshTokenMinutes());
    String refresh = refreshTokenService.issue(principal.getUserId(), refreshTtl);
    setRefreshCookie(res, refresh, refreshTtl);

    TokenResponse body = new TokenResponse(access, new UserSummary(
        principal.getUserId(), principal.getUsername(), principal.getName(), principal.getRoleKey(), principal.getOrgId()
    ));
    return ResponseEntity.ok(ApiResponse.ok(body));
  }

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
        user.orgId()
    );

    TokenResponse body = new TokenResponse(access, new UserSummary(
        user.userId(), user.username(), user.name(), user.roleKey(), user.orgId()
    ));
    return ResponseEntity.ok(ApiResponse.ok(body));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken,
      HttpServletResponse res
  ) {
    refreshTokenService.revoke(refreshToken);
    clearRefreshCookie(res);
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
