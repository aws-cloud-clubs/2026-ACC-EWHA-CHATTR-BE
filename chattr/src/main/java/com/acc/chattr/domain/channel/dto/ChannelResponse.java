package com.acc.chattr.domain.channel.dto;

import com.acc.chattr.domain.channel.entity.Channel;

import java.time.Instant;

public record ChannelResponse(
    String id,
    String workspaceId,
    String name,
    String description,
    String topic,
    String createdById,
    Instant createdAt
) {
    public static ChannelResponse from(Channel channel) {
        return new ChannelResponse(
            channel.getId(),
            channel.getWorkspaceId(),
            channel.getName(),
            channel.getDescription(),
            channel.getTopic(),
            channel.getCreatedById(),
            channel.getCreatedAt()
        );
    }
}
