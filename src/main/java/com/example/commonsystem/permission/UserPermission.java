package com.example.commonsystem.permission;

import java.util.List;

public record UserPermission(String screenKey, List<String> actions) {}
