package com.example.commonsystem.notification.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.notification.channel.NotificationChannel;
import com.example.commonsystem.notification.mapper.NotificationPreferenceMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 알림 라우터.
 * 인앱 알림은 항상 생성하고, 외부 채널(이메일, SMS 등)은
 * 사용자의 동의 + 선호도 + 채널 가용 여부를 모두 확인한 후 발송한다.
 */
@Slf4j
@Service
public class NotificationRouter {

    private final NotificationService notificationService;
    private final NotificationPreferenceMapper prefMapper;
    private final Map<String, NotificationChannel> channelMap;
    private final TenantContextHolder tenantCtx;

    public NotificationRouter(NotificationService notificationService,
                              NotificationPreferenceMapper prefMapper,
                              List<NotificationChannel> channels,
                              TenantContextHolder tenantCtx) {
        this.notificationService = notificationService;
        this.prefMapper = prefMapper;
        this.channelMap = channels.stream()
                .collect(Collectors.toMap(NotificationChannel::channelId, Function.identity()));
        this.tenantCtx = tenantCtx;
    }

    /**
     * 알림을 라우팅한다.
     * 1) 인앱 알림은 무조건 생성
     * 2) 외부 채널은 동의 + 활성화 + 채널 가용 시에만 발송
     *
     * @param userId    수신자 ID
     * @param tenantId  테넌트 ID
     * @param type      알림 유형 (COMMENT, PASSWORD_RESET, ...)
     * @param title     제목
     * @param message   본문 (HTML 가능)
     * @param link      인앱 이동 링크 (nullable)
     * @param recipient 외부 채널 수신 주소 (이메일, 전화번호 등, nullable)
     */
    public void route(long userId, Long tenantId, String type,
                      String title, String message, String link, String recipient) {
        // 1. 인앱 알림 (항상)
        notificationService.create(tenantId, userId, type, title, message, link);

        // 2. 외부 채널 발송
        if (recipient == null || recipient.isBlank()) return;

        for (NotificationChannel channel : channelMap.values()) {
            if (!channel.isAvailable()) continue;

            boolean allowed = prefMapper.isChannelEnabled(tenantId, userId, channel.channelId());
            if (!allowed) {
                log.debug("알림 스킵: userId={}, channel={} (동의 미완료 또는 비활성)", userId, channel.channelId());
                continue;
            }

            try {
                channel.send(recipient, title, message);
                log.info("알림 발송: userId={}, channel={}, type={}", userId, channel.channelId(), type);
            } catch (Exception e) {
                log.error("알림 발송 실패: userId={}, channel={}, error={}", userId, channel.channelId(), e.getMessage());
            }
        }
    }
}
