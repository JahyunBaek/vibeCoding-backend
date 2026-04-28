package com.example.commonsystem.approval.mapper;

import java.time.Instant;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.DocumentDetail;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.DocumentListRow;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.DocumentStepRow;
import com.example.commonsystem.approval.dto.ApprovalDocumentDtos.HistoryRow;
import com.example.commonsystem.approval.dto.ApprovalLineTemplateDtos.StepRequest;

@Mapper
public interface ApprovalDocumentMapper {

  // --- 문서 CRUD ---

  void insertDocument(@Param("tenantId") long tenantId,
                       @Param("documentNo") String documentNo,
                       @Param("approvalCode") String approvalCode,
                       @Param("businessType") String businessType,
                       @Param("businessId") String businessId,
                       @Param("title") String title,
                       @Param("body") String body,
                       @Param("requesterUserId") long requesterUserId,
                       @Param("requesterDepartmentId") Long requesterDepartmentId,
                       @Param("supervisingDepartmentId") Long supervisingDepartmentId);

  /** 방금 insert한 document_id (currval) */
  long currvalDocumentId();

  void insertStep(@Param("documentId") long documentId, @Param("s") StepRequest step);

  DocumentDetail findDetail(@Param("documentId") long documentId,
                             @Param("tenantId") long tenantId);

  List<DocumentStepRow> findSteps(@Param("documentId") long documentId);

  List<HistoryRow> findHistory(@Param("documentId") long documentId);

  /** 다음 미처리 단계 조회 (step_order ASC 최상위 PENDING) */
  DocumentStepRow findNextPendingStep(@Param("documentId") long documentId);

  /**
   * 상태 조건부 step 승인/반려 — 동시성 제어의 핵심.
   * 반환값이 0이면 이미 처리된 것 (다른 사용자가 먼저 처리).
   */
  int actOnStepIfPending(@Param("stepId") long stepId,
                          @Param("documentId") long documentId,
                          @Param("newStatus") String newStatus,
                          @Param("actedByUserId") long actedByUserId,
                          @Param("comment") String comment);

  /** 다음 단계로 진행 또는 완료 처리 */
  int updateDocumentStatus(@Param("documentId") long documentId,
                            @Param("tenantId") long tenantId,
                            @Param("status") String status,
                            @Param("currentStepOrder") Integer currentStepOrder,
                            @Param("completedAt") Instant completedAt);

  /** 회수/취소 */
  int cancelOrWithdraw(@Param("documentId") long documentId,
                        @Param("tenantId") long tenantId,
                        @Param("expectedStatus") String expectedStatus,
                        @Param("newStatus") String newStatus,
                        @Param("requesterUserId") long requesterUserId,
                        @Param("now") Instant now);

  // --- 목록 ---

  List<DocumentListRow> findInbox(@Param("tenantId") long tenantId,
                                    @Param("userId") long userId,
                                    @Param("inbox") String inbox,
                                    @Param("approvalCode") String approvalCode,
                                    @Param("status") String status,
                                    @Param("keyword") String keyword,
                                    @Param("fromDate") Instant fromDate,
                                    @Param("toDate") Instant toDate,
                                    @Param("limit") int limit,
                                    @Param("offset") int offset);

  long countInbox(@Param("tenantId") long tenantId,
                   @Param("userId") long userId,
                   @Param("inbox") String inbox,
                   @Param("approvalCode") String approvalCode,
                   @Param("status") String status,
                   @Param("keyword") String keyword,
                   @Param("fromDate") Instant fromDate,
                   @Param("toDate") Instant toDate);

  /** 사용자가 특정 단계를 승인할 권한이 있는지 판별 */
  int countActableUsers(@Param("stepId") long stepId,
                         @Param("userId") long userId);

  // --- 이력 ---

  void insertHistory(@Param("tenantId") long tenantId,
                      @Param("documentId") long documentId,
                      @Param("stepId") Long stepId,
                      @Param("actionType") String actionType,
                      @Param("actionBy") long actionBy,
                      @Param("actionComment") String actionComment,
                      @Param("beforeStatus") String beforeStatus,
                      @Param("afterStatus") String afterStatus);

  // --- 문서번호 시퀀스 ---

  /** 해당 테넌트+월의 최대 sequence */
  Integer findMaxDocumentSeq(@Param("tenantId") long tenantId,
                              @Param("yyyymm") String yyyymm);
}
