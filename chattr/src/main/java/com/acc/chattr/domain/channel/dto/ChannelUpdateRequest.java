package com.acc.chattr.domain.channel.dto;

import jakarta.validation.constraints.NotBlank;

public record ChannelUpdateRequest(
    @NotBlank String name,
    String description,
    String topic
) {}
