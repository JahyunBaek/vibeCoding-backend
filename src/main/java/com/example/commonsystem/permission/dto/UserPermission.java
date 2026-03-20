package com.example.commonsystem.permission.dto;

import java.util.List;

public record UserPermission(String screenKey, List<String> actions) {}
