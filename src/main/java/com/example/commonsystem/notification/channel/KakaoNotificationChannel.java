package com.example.commonsystem.notification.channel;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 카카오 알림톡 채널 (Stub 구현).
 * 실제 발송은 카카오 비즈메시지 API 연동 시 구현한다.
 */
@Slf4j
@Component
public class KakaoNotificationChannel implements NotificationChannel {

    @Override
    public String channelId() {
        return "KAKAO";
    }

    @Override
    public boolean isAvailable() {
        return false; // 카카오 연동 전까지 비활성
    }

    @Override
    public void send(String recipient, String title, String message) {
        log.info("[Kakao Stub] to={}, title={}, message={}", recipient, title, message);
    }
}
