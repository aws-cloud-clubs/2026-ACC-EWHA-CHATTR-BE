package com.acc.chattr.domain.workspace.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkspaceCreateRequest(
    @NotBlank String name
) {}
