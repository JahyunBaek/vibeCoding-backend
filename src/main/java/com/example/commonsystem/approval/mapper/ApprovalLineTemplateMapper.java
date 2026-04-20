package com.example.commonsystem.approval.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.StepRequest;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.StepRow;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.TemplateDetail;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.TemplateListRow;

@Mapper
public interface ApprovalLineTemplateMapper {

  List<TemplateListRow> findMyTemplates(@Param("tenantId") long tenantId,
                                          @Param("userId") long userId,
                                          @Param("approvalCode") String approvalCode);

  TemplateDetail findDetail(@Param("templateId") long templateId,
                             @Param("tenantId") long tenantId,
                             @Param("userId") long userId);

  TemplateDetail findDefault(@Param("tenantId") long tenantId,
                              @Param("userId") long userId,
                              @Param("approvalCode") String approvalCode);

  List<StepRow> findSteps(@Param("templateId") long templateId);

  void insertTemplate(@Param("tenantId") long tenantId,
                       @Param("userId") long userId,
                       @Param("approvalCode") String approvalCode,
                       @Param("templateName") String templateName,
                       @Param("defaultYn") boolean defaultYn);

  /** 방금 insert한 template_id (currval) */
  long currvalTemplateId();

  void insertStep(@Param("templateId") long templateId,
                   @Param("s") StepRequest step);

  int updateTemplate(@Param("templateId") long templateId,
                      @Param("tenantId") long tenantId,
                      @Param("userId") long userId,
                      @Param("templateName") String templateName,
                      @Param("defaultYn") boolean defaultYn,
                      @Param("activeYn") boolean activeYn);

  int deleteTemplate(@Param("templateId") long templateId,
                      @Param("tenantId") long tenantId,
                      @Param("userId") long userId);

  void deleteStepsByTemplate(@Param("templateId") long templateId);

  /** 해당 유저+코드의 default를 전부 해제 (UNSET) */
  int unsetDefault(@Param("tenantId") long tenantId,
                    @Param("userId") long userId,
                    @Param("approvalCode") String approvalCode);
}
