package com.example.commonsystem.tenant.dto;

public class TenantCreateCommand {
  private String tenantKey;
  private String tenantName;
  private String planType;
  private boolean active;
  private Long tenantId; // MyBatis useGeneratedKeys

  public TenantCreateCommand() {}

  public TenantCreateCommand(String tenantKey, String tenantName, String planType, boolean active) {
    this.tenantKey  = tenantKey;
    this.tenantName = tenantName;
    this.planType   = planType;
    this.active     = active;
  }

  public String getTenantKey()  { return tenantKey; }
  public String getTenantName() { return tenantName; }
  public String getPlanType()   { return planType; }
  public boolean isActive()     { return active; }
  public Long getTenantId()     { return tenantId; }
  public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
