package com.example.commonsystem.notification.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.notification.domain.Notification;
import com.example.commonsystem.notification.service.NotificationService;
import com.example.commonsystem.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "알림", description = "알림 조회 및 읽음 처리")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "알림 목록 조회", description = "현재 사용자의 최근 20개 알림을 조회합니다.")
    @GetMapping
    public ApiResponse<List<Notification>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(notificationService.list(principal.getUserId()));
    }

    @Operation(summary = "읽지 않은 알림 수 조회")
    @GetMapping("/unread-count")
    public ApiResponse<Integer> unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(notificationService.unreadCount(principal.getUserId()));
    }

    @Operation(summary = "알림 읽음 처리")
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable long id,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(id, principal.getUserId());
        return ApiResponse.ok();
    }

    @Operation(summary = "모든 알림 읽음 처리")
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getUserId());
        return ApiResponse.ok();
    }
}
