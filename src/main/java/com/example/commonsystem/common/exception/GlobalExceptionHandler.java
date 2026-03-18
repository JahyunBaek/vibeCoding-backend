package com.example.commonsystem.common.exception;

import com.example.commonsystem.common.ApiResponse;
import com.example.commonsystem.common.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<Void>> handleApp(AppException e) {
    log.warn("AppException code={}, message={}", e.getCode(), e.getMessage());
    HttpStatus status = ErrorCode.CONFLICT.equals(e.getCode())
        ? HttpStatus.CONFLICT
        : HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status)
        .body(ApiResponse.fail(e.getCode(), e.getMessage()));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
  public ResponseEntity<ApiResponse<Void>> handleValidation(Exception e) {
    // 검증 에러는 필요하면 자세히
    log.warn("Validation error", e);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.fail(ErrorCode.VALIDATION, "Validation error"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleAny(Exception e) {
    // ✅ 이게 핵심: 500은 반드시 스택트레이스 찍기
    log.error("Unhandled exception", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.fail(ErrorCode.INTERNAL, "Internal server error"));
  }
}