package com.example.commonsystem.agent.controller;

import com.example.commonsystem.agent.dto.AgentDtos.ChatRequest;
import com.example.commonsystem.agent.dto.AgentDtos.ChatResponse;
import com.example.commonsystem.agent.dto.AgentDtos.DatasetInfo;
import com.example.commonsystem.agent.dto.AgentDtos.ProviderInfo;
import com.example.commonsystem.agent.service.AgentChatProvider;
import com.example.commonsystem.agent.service.MockChatProvider;
import com.example.commonsystem.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Agent Chat", description = "AI Agent 채팅 (Gemini 실연동 + Mock)")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/agent")
public class AgentChatController {

    private final List<AgentChatProvider> chatProviders;
    private final MockChatProvider mockChatProvider;

    private static final Map<String, String> PROVIDER_NAMES = Map.of(
            "gemini", "Gemini",
            "chatgpt", "ChatGPT",
            "claude", "Claude"
    );

    // ── 사용 가능한 AI Agent 목록 ──
    @Operation(summary = "Agent 목록 조회")
    @GetMapping("/providers")
    public ApiResponse<List<ProviderInfo>> providers() {
        Map<String, AgentChatProvider> liveMap = chatProviders.stream()
                .collect(Collectors.toMap(AgentChatProvider::providerId, Function.identity()));

        List<ProviderInfo> list = List.of(
            buildProviderInfo("gemini", "Gemini", "gemini-3.0-flash",
                    "Google의 멀티모달 AI", "google", liveMap),
            buildProviderInfo("chatgpt", "ChatGPT", "gpt-4o",
                    "OpenAI의 대화형 AI", "openai", liveMap),
            buildProviderInfo("claude", "Claude", "claude-sonnet-4-20250514",
                    "Anthropic의 안전한 AI", "anthropic", liveMap)
        );
        return ApiResponse.ok(list);
    }

    // ── 분석 가능한 샘플 데이터셋 목록 ──
    @Operation(summary = "분석 가능 데이터셋 목록")
    @GetMapping("/datasets")
    public ApiResponse<List<DatasetInfo>> datasets() {
        List<DatasetInfo> list = List.of(
            new DatasetInfo("patients", "환자 데이터",
                    "20명의 환자 정보 (상태, 진료과, 혈액형 등)", 20),
            new DatasetInfo("trials", "임상시험 데이터",
                    "12건의 임상시험 정보 (단계, 상태, 등록현황 등)", 12)
        );
        return ApiResponse.ok(list);
    }

    // ── 채팅 메시지 전송 ──
    @Operation(summary = "Agent에 메시지 전송")
    @PostMapping("/chat")
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        AgentChatProvider liveProvider = findLiveProvider(request.provider());

        boolean useLive = liveProvider != null && liveProvider.isLive();
        String reply;

        if (useLive) {
            reply = liveProvider.chat(request.dataset(), request.message());
        } else {
            String providerName = PROVIDER_NAMES.getOrDefault(request.provider(), request.provider());
            reply = mockChatProvider.chat(providerName, request.dataset(), request.message());
        }

        ChatResponse response = new ChatResponse(
                UUID.randomUUID().toString(),
                request.provider(),
                request.dataset(),
                reply,
                Instant.now().toString(),
                !useLive
        );
        return ApiResponse.ok(response);
    }

    private AgentChatProvider findLiveProvider(String providerId) {
        return chatProviders.stream()
                .filter(p -> p.providerId().equals(providerId))
                .findFirst()
                .orElse(null);
    }

    private ProviderInfo buildProviderInfo(String id, String name, String model,
                                           String description, String icon,
                                           Map<String, AgentChatProvider> liveMap) {
        AgentChatProvider provider = liveMap.get(id);
        boolean live = provider != null && provider.isLive();
        return new ProviderInfo(id, name, model, description, icon, true, !live);
    }
}
