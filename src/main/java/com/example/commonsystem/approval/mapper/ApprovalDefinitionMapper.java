package com.example.commonsystem.approval.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.AuthorityRuleRow;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.CreateAuthorityRuleRequest;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.CreateRequest;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.CreateRequiredStepRequest;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.DefinitionDetail;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.DefinitionListRow;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.RequiredStepRow;
import com.example.commonsystem.approval.dto.ApprovalDefinitionDtos.UpdateRequest;

@Mapper
public interface ApprovalDefinitionMapper {

  List<DefinitionListRow> findAll(@Param("tenantId") Long tenantId,
                                   @Param("activeOnly") boolean activeOnly,
                                   @Param("keyword") String keyword);

  DefinitionDetail findByCode(@Param("tenantId") Long tenantId,
                               @Param("approvalCode") String approvalCode);

  DefinitionDetail findById(@Param("definitionId") long definitionId,
                             @Param("tenantId") Long tenantId);

  int existsByCode(@Param("tenantId") long tenantId,
                    @Param("approvalCode") String approvalCode);

  long insert(@Param("tenantId") long tenantId,
               @Param("req") CreateRequest req,
               @Param("userId") long userId);

  int update(@Param("definitionId") long definitionId,
              @Param("tenantId") long tenantId,
              @Param("req") UpdateRequest req,
              @Param("userId") long userId);

  int deleteById(@Param("definitionId") long definitionId,
                  @Param("tenantId") long tenantId);

  // Authority rules
  List<AuthorityRuleRow> findAuthorityRules(@Param("tenantId") long tenantId,
                                             @Param("approvalCode") String approvalCode);

  void insertAuthorityRule(@Param("tenantId") long tenantId,
                            @Param("approvalCode") String approvalCode,
                            @Param("req") CreateAuthorityRuleRequest req);

  int deleteAuthorityRule(@Param("ruleId") long ruleId,
                           @Param("tenantId") long tenantId);

  // Required steps (정책 필수 결재 단계)
  List<RequiredStepRow> findRequiredSteps(@Param("definitionId") long definitionId);

  /** approval_code 로 조회 (테넌트 격리 — definition 조인) */
  List<RequiredStepRow> findRequiredStepsByCode(@Param("tenantId") long tenantId,
                                                  @Param("approvalCode") String approvalCode);

  Integer findMaxRequiredStepOrder(@Param("definitionId") long definitionId);

  void insertRequiredStep(@Param("definitionId") long definitionId,
                           @Param("stepOrder") int stepOrder,
                           @Param("req") CreateRequiredStepRequest req);

  int deleteRequiredStep(@Param("requiredStepId") long requiredStepId,
                          @Param("definitionId") long definitionId);

  /** definition_id 가 해당 tenant 소속인지 확인 */
  int countDefinitionByIdAndTenant(@Param("definitionId") long definitionId,
                                     @Param("tenantId") long tenantId);
}
