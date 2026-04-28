package com.example.commonsystem.user.dto;

/**
 * 사용자 디렉토리 검색 결과.
 * 결재선/멘션 등에서 사용자를 선택할 때 보여주는 가벼운 정보.
 */
public record UserDirectoryRow(
    long userId,
    String username,
    String name,
    String roleKey,
    Long orgId,
    String orgName
) {}
