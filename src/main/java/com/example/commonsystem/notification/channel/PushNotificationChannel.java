package com.example.commonsystem.notification.channel;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 앱 푸시 알림 채널 (Stub 구현).
 * Firebase Cloud Messaging(FCM) / APNs 연동 시 구현한다.
 */
@Slf4j
@Component
public class PushNotificationChannel implements NotificationChannel {

    @Override
    public String channelId() {
        return "PUSH";
    }

    @Override
    public boolean isAvailable() {
        return false; // FCM 연동 전까지 비활성
    }

    @Override
    public void send(String recipient, String title, String message) {
        log.info("[Push Stub] deviceToken={}, title={}, message={}", recipient, title, message);
    }
}
