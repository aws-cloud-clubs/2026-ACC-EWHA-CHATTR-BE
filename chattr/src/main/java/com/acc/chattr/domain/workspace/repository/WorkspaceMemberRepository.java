package com.acc.chattr.domain.workspace.repository;

import com.acc.chattr.domain.workspace.entity.WorkspaceMember;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository {
    void save(WorkspaceMember member);
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId);
    List<WorkspaceMember> findByWorkspaceId(String workspaceId);
    List<WorkspaceMember> findByUserId(String userId);
    void deleteAllByWorkspaceId(String workspaceId);
}
