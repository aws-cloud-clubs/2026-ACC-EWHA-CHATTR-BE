package com.acc.chattr.domain.workspace.dto;

import jakarta.validation.constraints.NotBlank;

public record InviteRequest(
    @NotBlank String userId
) {}
