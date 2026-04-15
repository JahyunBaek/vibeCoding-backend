package com.example.commonsystem.invitation.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.audit.service.AuditService;
import com.example.commonsystem.common.EmailService;
import com.example.commonsystem.common.ErrorCode;
import com.example.commonsystem.common.exception.AppException;
import com.example.commonsystem.invitation.domain.Invitation;
import com.example.commonsystem.invitation.dto.InvitationDtos.InvitationListRow;
import com.example.commonsystem.invitation.mapper.InvitationMapper;
import com.example.commonsystem.tenant.domain.Tenant;
import com.example.commonsystem.tenant.mapper.TenantMapper;
import com.example.commonsystem.tenant.service.TenantConfigService;
import com.example.commonsystem.user.dto.UserCreateCommand;
import com.example.commonsystem.user.mapper.UserMapper;

@Service
public class InvitationService {

    private final InvitationMapper invitationMapper;
    private final TenantMapper tenantMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditService auditService;
    private final TenantConfigService tenantConfigService;

    public InvitationService(InvitationMapper invitationMapper, TenantMapper tenantMapper,
            UserMapper userMapper, PasswordEncoder passwordEncoder,
            EmailService emailService, AuditService auditService,
            TenantConfigService tenantConfigService) {
        this.invitationMapper = invitationMapper;
        this.tenantMapper = tenantMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.auditService = auditService;
        this.tenantConfigService = tenantConfigService;
    }

    /**
     * 사용자를 초대한다. UUID 토큰을 생성하고 DB에 저장한 뒤 이메일을 발송한다.
     * @return 생성된 초대 토큰
     */
    @Transactional
    public String invite(long tenantId, String email, String roleKey, long invitedBy) {
        Tenant tenant = tenantMapper.findById(tenantId);
        if (tenant == null) {
            throw new AppException(ErrorCode.NOT_FOUND, "테넌트를 찾을 수 없습니다.");
        }

        String effectiveRoleKey = (roleKey == null || roleKey.isBlank()) ? "USER" : roleKey;
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

        invitationMapper.insert(tenantId, email, effectiveRoleKey, token, invitedBy, expiresAt);
        String locale = tenantConfigService.getLocale(tenantId);
        emailService.sendInvitation(email, tenant.tenantName(), token, locale);

        auditService.log("CREATE", "INVITATION", email,
                "tenantId=" + tenantId + ", roleKey=" + effectiveRoleKey);
        return token;
    }

    /**
     * 토큰이 유효한지 검증한다 (PENDING + 미만료).
     * @return 유효하면 Invitation, 아니면 null
     */
    public Invitation validateToken(String token) {
        if (token == null || token.isBlank()) return null;
        Invitation inv = invitationMapper.findByToken(token);
        if (inv == null) return null;
        if (!"PENDING".equals(inv.status())) return null;
        if (Instant.now().isAfter(inv.expiresAt())) {
            // 만료된 초대는 상태를 EXPIRED로 업데이트
            invitationMapper.updateStatus(inv.invitationId(), "EXPIRED");
            return null;
        }
        return inv;
    }

    /**
     * 초대를 수락하고 계정을 생성한다.
     */
    @Transactional
    public void acceptInvitation(String token, String username, String password, String name) {
        Invitation inv = validateToken(token);
        if (inv == null) {
            throw new AppException(ErrorCode.VALIDATION, "유효하지 않거나 만료된 초대 토큰입니다.");
        }

        // username 중복 체크
        if (userMapper.findByUsername(username) != null) {
            throw new AppException(ErrorCode.CONFLICT, "이미 사용 중인 아이디입니다.");
        }

        String hash = passwordEncoder.encode(password);
        userMapper.insert(new UserCreateCommand(
                username, hash, name, inv.roleKey(), null, inv.tenantId(), true));

        invitationMapper.updateStatus(inv.invitationId(), "ACCEPTED");

        auditService.log("SIGNUP", "USER", username,
                "tenantId=" + inv.tenantId() + ", invitationId=" + inv.invitationId());
    }

    /**
     * 테넌트별 초대 목록을 조회한다.
     */
    public List<InvitationListRow> listByTenant(long tenantId) {
        return invitationMapper.findByTenant(tenantId);
    }
}
