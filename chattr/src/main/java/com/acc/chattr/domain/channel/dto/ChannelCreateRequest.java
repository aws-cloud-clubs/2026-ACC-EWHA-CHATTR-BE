package com.acc.chattr.domain.channel.dto;

import jakarta.validation.constraints.NotBlank;

public record ChannelCreateRequest(
    @NotBlank String name,
    String description,
    String topic
) {}
