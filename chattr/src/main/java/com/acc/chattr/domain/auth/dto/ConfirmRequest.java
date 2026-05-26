package com.acc.chattr.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ConfirmRequest(
    @NotBlank @Email String email,
    @NotBlank String code
) {}
