package com.acc.chattr.domain.channel.dto;

import jakarta.validation.constraints.NotBlank;

public record AddMemberRequest(
    @NotBlank String userId
) {}
