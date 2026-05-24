package com.acc.chattr.domain.user.dto;

import com.acc.chattr.domain.user.entity.User;

import java.time.Instant;

public record UserResponse(
    String id,
    String email,
    String nickname,
    boolean online,
    Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.isOnline(),
            user.getCreatedAt()
        );
    }
}
