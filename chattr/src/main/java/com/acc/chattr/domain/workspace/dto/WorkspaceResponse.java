package com.acc.chattr.domain.workspace.dto;

import com.acc.chattr.domain.workspace.entity.Workspace;
import com.acc.chattr.domain.workspace.entity.WorkspaceRole;

import java.time.Instant;

public record WorkspaceResponse(
    String id,
    String name,
    WorkspaceRole myRole,
    Instant createdAt
) {
    public static WorkspaceResponse from(Workspace workspace, WorkspaceRole myRole) {
        return new WorkspaceResponse(
            workspace.getId(),
            workspace.getName(),
            myRole,
            workspace.getCreatedAt()
        );
    }
}
