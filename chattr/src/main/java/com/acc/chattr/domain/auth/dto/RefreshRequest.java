package com.acc.chattr.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank String refreshToken,
    @NotBlank String username
) {}
