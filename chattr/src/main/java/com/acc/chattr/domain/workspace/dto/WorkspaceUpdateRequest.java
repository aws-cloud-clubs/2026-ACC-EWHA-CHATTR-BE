package com.acc.chattr.domain.workspace.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkspaceUpdateRequest(
    @NotBlank String name
) {}
