package com.example.commonsystem.common;

public final class ErrorCode {
  private ErrorCode() {}

  public static final String UNAUTHORIZED = "AUTH_401";
  public static final String FORBIDDEN = "AUTH_403";
  public static final String NOT_FOUND = "COMMON_404";
  public static final String VALIDATION = "COMMON_400";
  public static final String CONFLICT = "COMMON_409";
  public static final String INTERNAL = "COMMON_500";
}
