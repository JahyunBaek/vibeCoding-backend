package com.example.commonsystem.notification.channel;

/**
 * 알림 채널 인터페이스.
 * EMAIL, SMS, KAKAO, PUSH 등 각 채널이 이 인터페이스를 구현한다.
 */
public interface NotificationChannel {

    /** 채널 ID (e.g. "EMAIL", "SMS", "KAKAO", "PUSH") */
    String channelId();

    /** 해당 채널이 현재 사용 가능한지 (설정 완료 여부) */
    boolean isAvailable();

    /**
     * 알림을 발송한다.
     *
     * @param recipient 수신자 정보 (이메일 주소, 전화번호 등 채널에 따라 다름)
     * @param title     알림 제목
     * @param message   알림 본문
     */
    void send(String recipient, String title, String message);
}
