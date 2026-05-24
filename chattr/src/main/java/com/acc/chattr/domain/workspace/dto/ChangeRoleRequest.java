package com.acc.chattr.domain.workspace.dto;

import com.acc.chattr.domain.workspace.entity.WorkspaceRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
    @NotBlank String userId,
    @NotNull WorkspaceRole role
) {}
