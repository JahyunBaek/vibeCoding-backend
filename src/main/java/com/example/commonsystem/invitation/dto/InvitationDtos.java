package com.example.commonsystem.invitation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InvitationDtos {

    public record InviteRequest(
        @NotBlank @Email String email,
        String roleKey  // defaults to USER if null
    ) {}

    public record InvitationListRow(
        long invitationId,
        long tenantId,
        String email,
        String roleKey,
        String status,
        long invitedBy,
        String invitedByUsername,
        String createdAt,
        String expiresAt
    ) {}

    public record InvitationInfoResponse(
        String email,
        String tenantName
    ) {}

    public record SignupRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 50) String name
    ) {}
}
