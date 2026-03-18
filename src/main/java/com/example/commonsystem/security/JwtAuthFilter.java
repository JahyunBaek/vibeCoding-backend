package com.example.commonsystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    log.info("shouldNotFilter: {}", request.getRequestURI());
    return request.getRequestURI().equals("/api/auth/refresh");
  }
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String auth = request.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7);
      try {
        Claims claims = jwtService.parse(token);
        String username = claims.getSubject();
        long userId = ((Number) claims.get("uid")).longValue();
        String role = (String) claims.get("role");
        String name = (String) claims.get("name");
        Object orgIdObj = claims.get("orgId");
        Long orgId = orgIdObj == null ? null : ((Number) orgIdObj).longValue();

        UserPrincipal principal = new UserPrincipal(userId, username, "", role, name, orgId, true);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal, null, principal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JwtException e) {
        log.warn("JWT expired", e);
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
            "Bearer error=\"invalid_token\", error_description=\"expired\"");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter()
            .write("{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"Token expired\"}}");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
