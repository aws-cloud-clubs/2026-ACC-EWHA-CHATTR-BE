package com.acc.chattr.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterDeviceRequest(
    @NotBlank String deviceId,
    @NotBlank String deviceName,
    @NotBlank String platform
) {}
