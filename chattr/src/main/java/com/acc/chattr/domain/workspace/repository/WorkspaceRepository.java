package com.acc.chattr.domain.workspace.repository;

import com.acc.chattr.domain.workspace.entity.Workspace;

import java.util.Optional;

public interface WorkspaceRepository {
    void save(Workspace workspace);
    Optional<Workspace> findById(String workspaceId);
}
