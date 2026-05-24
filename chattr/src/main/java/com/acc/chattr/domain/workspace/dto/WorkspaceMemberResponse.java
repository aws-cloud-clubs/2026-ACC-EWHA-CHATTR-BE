package com.acc.chattr.domain.workspace.dto;

import com.acc.chattr.domain.user.entity.User;
import com.acc.chattr.domain.workspace.entity.WorkspaceMember;
import com.acc.chattr.domain.workspace.entity.WorkspaceRole;

import java.time.Instant;

public record WorkspaceMemberResponse(
    String userId,
    String email,
    String nickname,
    boolean online,
    WorkspaceRole role,
    Instant joinedAt
) {
    public static WorkspaceMemberResponse from(WorkspaceMember member, User user) {
        return new WorkspaceMemberResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.isOnline(),
            member.getRole(),
            member.getCreatedAt()
        );
    }
}
