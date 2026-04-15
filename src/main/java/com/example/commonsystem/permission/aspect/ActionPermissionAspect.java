package com.example.commonsystem.permission.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.example.commonsystem.permission.annotation.RequiresAction;
import com.example.commonsystem.permission.service.PermissionService;

@Aspect
@Component
public class ActionPermissionAspect {

  private final PermissionService permissionService;

  public ActionPermissionAspect(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  @Before("@annotation(requiresAction)")
  public void checkPermission(RequiresAction requiresAction) {
    permissionService.checkAction(requiresAction.screen(), requiresAction.action());
  }
}
