package com.example.commonsystem.notification.channel;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * SMS 알림 채널 (Stub 구현).
 * 실제 SMS 발송은 외부 API(AWS SNS, NHN Toast 등) 연동 시 구현한다.
 */
@Slf4j
@Component
public class SmsNotificationChannel implements NotificationChannel {

    @Override
    public String channelId() {
        return "SMS";
    }

    @Override
    public boolean isAvailable() {
        return false; // SMS 연동 전까지 비활성
    }

    @Override
    public void send(String recipient, String title, String message) {
        log.info("[SMS Stub] to={}, title={}, message={}", recipient, title, message);
    }
}
