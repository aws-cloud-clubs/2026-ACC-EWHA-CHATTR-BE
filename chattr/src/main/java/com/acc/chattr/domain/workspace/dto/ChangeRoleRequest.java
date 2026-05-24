package com.acc.chattr.domain.workspace.dto;

import com.acc.chattr.domain.workspace.entity.WorkspaceRole;

public record ChangeRoleRequest(String userId, WorkspaceRole role) {}
