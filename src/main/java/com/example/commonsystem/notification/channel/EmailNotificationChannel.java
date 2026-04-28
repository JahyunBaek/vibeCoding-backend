package com.example.commonsystem.notification.channel;

import org.springframework.stereotype.Component;

import com.example.commonsystem.common.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이메일 알림 채널 구현체.
 * 기존 EmailService를 위임하여 이메일을 발송한다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class EmailNotificationChannel implements NotificationChannel {

    private final EmailService emailService;

    @Override
    public String channelId() {
        return "EMAIL";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void send(String recipient, String title, String message) {
        try {
            emailService.sendGeneric(recipient, title, message);
        } catch (Exception e) {
            log.error("이메일 알림 발송 실패: to={}, title={}, error={}", recipient, title, e.getMessage());
        }
    }
}
