package com.example.commonsystem.invitation.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.invitation.domain.Invitation;
import com.example.commonsystem.invitation.dto.InvitationDtos.InvitationListRow;

@Mapper
public interface InvitationMapper {

    Invitation findByToken(@Param("token") String token);

    void insert(@Param("tenantId") long tenantId,
                @Param("email") String email,
                @Param("roleKey") String roleKey,
                @Param("token") String token,
                @Param("invitedBy") long invitedBy,
                @Param("expiresAt") java.time.Instant expiresAt);

    void updateStatus(@Param("invitationId") long invitationId,
                      @Param("status") String status);

    List<InvitationListRow> findByTenant(@Param("tenantId") long tenantId);
}
