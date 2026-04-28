package com.example.commonsystem.notification.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.common.TenantContextHolder;
import com.example.commonsystem.notification.channel.NotificationChannel;
import com.example.commonsystem.notification.dto.PreferenceDtos.PreferenceRow;
import com.example.commonsystem.notification.dto.PreferenceDtos.UpdateRequest;
import com.example.commonsystem.notification.mapper.NotificationPreferenceMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NotificationPreferenceService {

    private final NotificationPreferenceMapper prefMapper;
    private final List<NotificationChannel> channels;
    private final TenantContextHolder tenantCtx;

    /**
     * 사용자의 알림 채널 선호도 목록을 조회한다.
     * DB에 없는 채널도 기본값으로 포함하여 반환한다.
     */
    public List<PreferenceRow> getPreferences(long userId) {
        Long tenantId = tenantCtx.currentTenantId();
        List<Map<String, Object>> dbRows = prefMapper.findByUser(tenantId, userId);

        Map<String, Map<String, Object>> dbMap = dbRows.stream()
                .collect(Collectors.toMap(r -> (String) r.get("channel"), r -> r));

        Map<String, Boolean> availableMap = channels.stream()
                .collect(Collectors.toMap(NotificationChannel::channelId, NotificationChannel::isAvailable));

        return channels.stream()
                .map(ch -> {
                    Map<String, Object> row = dbMap.get(ch.channelId());
                    if (row != null) {
                        return new PreferenceRow(
                                ((Number) row.get("prefId")).longValue(),
                                ch.channelId(),
                                Boolean.TRUE.equals(row.get("enabled")),
                                Boolean.TRUE.equals(row.get("consented")),
                                null, // consentedAt은 상세 조회에서
                                ch.isAvailable()
                        );
                    }
                    return new PreferenceRow(0, ch.channelId(), false, false, null, ch.isAvailable());
                })
                .toList();
    }

    @Transactional
    public void updatePreference(long userId, UpdateRequest req) {
        Long tenantId = tenantCtx.currentTenantId();
        prefMapper.upsert(tenantId, userId, req.channel(), req.enabled(), req.consented());
    }
}
