package com.acc.chattr.domain.file.dto;

import jakarta.validation.constraints.NotBlank;

public record PresignRequest(
    @NotBlank String fileName,
    @NotBlank String contentType
) {}
