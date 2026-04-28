package com.example.commonsystem.notification.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.notification.dto.PreferenceDtos.PreferenceRow;
import com.example.commonsystem.notification.dto.PreferenceDtos.UpdateRequest;
import com.example.commonsystem.notification.service.NotificationPreferenceService;
import com.example.commonsystem.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "알림 설정", description = "알림 채널 선호도 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notification-preferences")
public class NotificationPreferenceController {

    private final NotificationPreferenceService prefService;

    @Operation(summary = "내 알림 채널 설정 조회")
    @GetMapping
    public ApiResponse<List<PreferenceRow>> getMyPreferences(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(prefService.getPreferences(principal.getUserId()));
    }

    @Operation(summary = "알림 채널 설정 변경")
    @PutMapping
    public ApiResponse<Void> updatePreference(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateRequest req) {
        prefService.updatePreference(principal.getUserId(), req);
        return ApiResponse.ok();
    }
}
