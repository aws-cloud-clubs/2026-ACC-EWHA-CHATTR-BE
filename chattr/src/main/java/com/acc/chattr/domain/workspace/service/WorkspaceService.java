package com.acc.chattr.domain.workspace.service;

import com.acc.chattr.common.code.BusinessErrorCode;
import com.acc.chattr.common.exception.BusinessException;
import com.acc.chattr.domain.user.entity.User;
import com.acc.chattr.domain.user.repository.UserRepository;
import com.acc.chattr.domain.workspace.dto.ChangeRoleRequest;
import com.acc.chattr.domain.workspace.dto.InviteRequest;
import com.acc.chattr.domain.workspace.dto.WorkspaceCreateRequest;
import com.acc.chattr.domain.workspace.dto.WorkspaceMemberResponse;
import com.acc.chattr.domain.workspace.dto.WorkspaceResponse;
import com.acc.chattr.domain.workspace.dto.WorkspaceUpdateRequest;
import com.acc.chattr.domain.workspace.entity.Workspace;
import com.acc.chattr.domain.workspace.entity.WorkspaceMember;
import com.acc.chattr.domain.workspace.entity.WorkspaceRole;
import com.acc.chattr.domain.workspace.repository.WorkspaceMemberRepository;
import com.acc.chattr.domain.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            WorkspaceMemberRepository workspaceMemberRepository,
                            UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
    }

    public WorkspaceResponse create(String cognitoSub, WorkspaceCreateRequest request) {
        User user = getUser(cognitoSub);
        Workspace workspace = Workspace.create(UUID.randomUUID().toString(), request.name());
        workspaceRepository.save(workspace);
        workspaceMemberRepository.save(WorkspaceMember.create(workspace.getId(), user.getId(), WorkspaceRole.ADMIN));
        return WorkspaceResponse.from(workspace, WorkspaceRole.ADMIN);
    }

    public List<WorkspaceResponse> getMyWorkspaces(String cognitoSub) {
        User user = getUser(cognitoSub);
        return workspaceMemberRepository.findByUserId(user.getId()).stream()
            .map(m -> workspaceRepository.findById(m.getWorkspaceId())
                .map(w -> WorkspaceResponse.from(w, m.getRole()))
                .orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }

    public WorkspaceResponse getWorkspace(String cognitoSub, String workspaceId) {
        User user = getUser(cognitoSub);
        Workspace workspace = getWorkspaceOrThrow(workspaceId);
        WorkspaceMember member = getMemberOrThrow(workspaceId, user.getId());
        return WorkspaceResponse.from(workspace, member.getRole());
    }

    public WorkspaceResponse update(String cognitoSub, String workspaceId, WorkspaceUpdateRequest request) {
        User user = getUser(cognitoSub);
        Workspace workspace = getWorkspaceOrThrow(workspaceId);
        WorkspaceMember member = getMemberOrThrow(workspaceId, user.getId());
        requireAdmin(member);
        workspace.rename(request.name());
        workspaceRepository.save(workspace);
        return WorkspaceResponse.from(workspace, member.getRole());
    }

    public void delete(String cognitoSub, String workspaceId) {
        User user = getUser(cognitoSub);
        Workspace workspace = getWorkspaceOrThrow(workspaceId);
        WorkspaceMember member = getMemberOrThrow(workspaceId, user.getId());
        requireAdmin(member);
        workspace.delete();
        workspaceRepository.save(workspace);
    }

    public List<WorkspaceMemberResponse> getMembers(String workspaceId) {
        getWorkspaceOrThrow(workspaceId);
        return workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
            .map(m -> userRepository.findById(m.getUserId())
                .map(u -> WorkspaceMemberResponse.from(m, u))
                .orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }

    public void invite(String cognitoSub, String workspaceId, InviteRequest request) {
        User currentUser = getUser(cognitoSub);
        getWorkspaceOrThrow(workspaceId);
        WorkspaceMember currentMember = getMemberOrThrow(workspaceId, currentUser.getId());
        requireAdmin(currentMember);

        userRepository.findById(request.userId())
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND));

        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, request.userId()).isPresent()) {
            throw new BusinessException(BusinessErrorCode.WORKSPACE_MEMBER_ALREADY_EXISTS);
        }

        workspaceMemberRepository.save(WorkspaceMember.create(workspaceId, request.userId(), WorkspaceRole.MEMBER));
    }

    public void acceptInvitation(String cognitoSub, String workspaceId) {
        User user = getUser(cognitoSub);
        getWorkspaceOrThrow(workspaceId);
        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId()).isPresent()) {
            throw new BusinessException(BusinessErrorCode.WORKSPACE_MEMBER_ALREADY_EXISTS);
        }
        workspaceMemberRepository.save(WorkspaceMember.create(workspaceId, user.getId(), WorkspaceRole.MEMBER));
    }

    public void changeRole(String cognitoSub, String workspaceId, ChangeRoleRequest request) {
        User currentUser = getUser(cognitoSub);
        getWorkspaceOrThrow(workspaceId);
        WorkspaceMember currentMember = getMemberOrThrow(workspaceId, currentUser.getId());
        requireAdmin(currentMember);

        WorkspaceMember targetMember = getMemberOrThrow(workspaceId, request.userId());
        targetMember.changeRole(request.role());
        workspaceMemberRepository.save(targetMember);
    }

    private User getUser(String cognitoSub) {
        return userRepository.findByCognitoSub(cognitoSub)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND));
    }

    private Workspace getWorkspaceOrThrow(String workspaceId) {
        return workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.WORKSPACE_NOT_FOUND));
    }

    private WorkspaceMember getMemberOrThrow(String workspaceId, String userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.WORKSPACE_MEMBER_NOT_FOUND));
    }

    private void requireAdmin(WorkspaceMember member) {
        if (!member.isAdmin()) {
            throw new BusinessException(BusinessErrorCode.NOT_WORKSPACE_ADMIN);
        }
    }
}
