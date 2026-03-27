package com.example.commonsystem.common;

import com.example.commonsystem.config.MailProperties;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final I18nService i18n;

    public EmailService(JavaMailSender mailSender, MailProperties mailProperties, I18nService i18n) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
        this.i18n = i18n;
    }

    /**
     * 비밀번호 재설정 이메일을 발송한다.
     */
    public void sendPasswordReset(String toEmail, String userName, String resetToken, String locale) {
        Locale loc = i18n.toLocale(locale);
        String resetUrl = mailProperties.baseUrl() + "/reset-password?token=" + resetToken;

        String subject = i18n.getMessage("email.passwordReset.subject", loc);
        String html = passwordResetTemplate(userName, resetUrl, loc);
        sendGeneric(toEmail, subject, html);
    }

    /**
     * 테넌트 초대 이메일을 발송한다.
     */
    public void sendInvitation(String toEmail, String tenantName, String inviteToken, String locale) {
        Locale loc = i18n.toLocale(locale);
        String inviteUrl = mailProperties.baseUrl() + "/signup?token=" + inviteToken;

        String subject = i18n.getMessage("email.invitation.subject", loc, tenantName);
        String html = invitationTemplate(tenantName, inviteUrl, loc);
        sendGeneric(toEmail, subject, html);
    }

    /**
     * HTML 이메일을 발송하는 저수준 메서드.
     * mailProperties.enabled()가 false이면 로그만 남기고 실제 발송하지 않는다.
     */
    public void sendGeneric(String to, String subject, String htmlBody) {
        if (!mailProperties.enabled()) {
            log.info("[Email disabled] To: {}, Subject: {}\n{}", to, subject, htmlBody);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailProperties.from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("[Email sent] To: {}, Subject: {}", to, subject);
        } catch (Exception e) {
            log.error("[Email failed] To: {}, Subject: {}, Error: {}", to, subject, e.getMessage(), e);
        }
    }

    // ── Email Templates ───────────────────────────────────────────

    private String fontFamily(Locale loc) {
        return "ko".equals(loc.getLanguage())
                ? "'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif"
                : "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";
    }

    private String passwordResetTemplate(String userName, String resetUrl, Locale loc) {
        return """
                <div style="font-family: %s; max-width: 600px; margin: 0 auto; padding: 20px;">
                  <h2 style="color: #1a1a1a;">%s</h2>
                  <p>%s</p>
                  <p>%s</p>
                  <div style="text-align: center; margin: 30px 0;">
                    <a href="%s"
                       style="background-color: #2563eb; color: #ffffff; padding: 12px 32px;
                              text-decoration: none; border-radius: 6px; font-size: 16px;">
                      %s
                    </a>
                  </div>
                  <p style="color: #6b7280; font-size: 13px;">
                    %s<br/>
                    <a href="%s">%s</a>
                  </p>
                  <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;" />
                  <p style="color: #9ca3af; font-size: 12px;">%s</p>
                </div>
                """.formatted(
                fontFamily(loc),
                i18n.getMessage("email.passwordReset.heading", loc),
                i18n.getMessage("email.passwordReset.greeting", loc, userName),
                i18n.getMessage("email.passwordReset.body", loc),
                resetUrl,
                i18n.getMessage("email.passwordReset.button", loc),
                i18n.getMessage("email.passwordReset.fallback", loc),
                resetUrl, resetUrl,
                i18n.getMessage("email.passwordReset.ignore", loc)
        );
    }

    private String invitationTemplate(String tenantName, String inviteUrl, Locale loc) {
        return """
                <div style="font-family: %s; max-width: 600px; margin: 0 auto; padding: 20px;">
                  <h2 style="color: #1a1a1a;">%s</h2>
                  <p><strong>%s</strong></p>
                  <p>%s</p>
                  <div style="text-align: center; margin: 30px 0;">
                    <a href="%s"
                       style="background-color: #2563eb; color: #ffffff; padding: 12px 32px;
                              text-decoration: none; border-radius: 6px; font-size: 16px;">
                      %s
                    </a>
                  </div>
                  <p style="color: #6b7280; font-size: 13px;">
                    %s<br/>
                    <a href="%s">%s</a>
                  </p>
                </div>
                """.formatted(
                fontFamily(loc),
                i18n.getMessage("email.invitation.heading", loc),
                i18n.getMessage("email.invitation.body", loc, tenantName),
                i18n.getMessage("email.invitation.action", loc),
                inviteUrl,
                i18n.getMessage("email.invitation.button", loc),
                i18n.getMessage("email.invitation.fallback", loc),
                inviteUrl, inviteUrl
        );
    }
}
