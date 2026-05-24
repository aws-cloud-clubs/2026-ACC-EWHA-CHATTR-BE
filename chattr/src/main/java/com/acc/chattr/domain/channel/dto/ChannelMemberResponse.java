package com.acc.chattr.domain.channel.dto;

import com.acc.chattr.domain.channel.entity.ChannelMember;
import com.acc.chattr.domain.user.entity.User;

import java.time.Instant;

public record ChannelMemberResponse(
    String userId,
    String email,
    String nickname,
    boolean online,
    Instant joinedAt
) {
    public static ChannelMemberResponse from(ChannelMember member, User user) {
        return new ChannelMemberResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.isOnline(),
            member.getCreatedAt()
        );
    }
}
