package com.example.commonsystem.agent.service;

/**
 * AI Agent 채팅 프로바이더 인터페이스.
 * 각 AI 서비스(Gemini, ChatGPT, Claude 등)가 이 인터페이스를 구현한다.
 */
public interface AgentChatProvider {

    /** 이 프로바이더가 처리할 수 있는 provider ID (e.g. "gemini") */
    String providerId();

    /** 실제 AI API와 연동되어 있는지 여부 */
    boolean isLive();

    /** 채팅 메시지 처리 */
    String chat(String dataset, String message);
}
