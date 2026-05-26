package com.acc.chattr.domain.workspace.service;

import com.acc.chattr.common.code.BusinessErrorCode;
import com.acc.chattr.common.exception.BusinessException;
import com.acc.chattr.domain.channel.repository.ChannelMemberRepository;
import com.acc.chattr.domain.channel.repository.ChannelRepository;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final UserRepository userRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            WorkspaceMemberRepository workspaceMemberRepository,
                            ChannelRepository channelRepository,
                            ChannelMemberRepository channelMemberRepository,
                            UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.channelRepository = channelRepository;
        this.channelMemberRepository = channelMemberRepository;
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
        List<WorkspaceMember> memberships = workspaceMemberRepository.findByUserId(user.getId());
        List<String> workspaceIds = memberships.stream().map(WorkspaceMember::getWorkspaceId).toList();
        Map<String, Workspace> workspaceMap = workspaceRepository.findAllByIds(workspaceIds).stream()
            .collect(Collectors.toMap(Workspace::getId, w -> w));
        return memberships.stream()
            .map(m -> {
                Workspace w = workspaceMap.get(m.getWorkspaceId());
                if (w == null) return null;
                return WorkspaceResponse.from(w, m.getRole());
            })
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

        // 채널 멤버 → 채널(soft delete) → 워크스페이스 멤버 순으로 정리
        // findAllIdsByWorkspaceId: 이미 삭제된 채널 포함 전체 조회 → 고아 channel-member 방지
        List<String> channelIds = channelRepository.findAllIdsByWorkspaceId(workspaceId);
        for (String channelId : channelIds) {
            channelMemberRepository.deleteAllByChannelId(channelId);
        }
        // 활성 채널만 soft delete (삭제된 채널은 이미 처리됨)
        channelRepository.findByWorkspaceId(workspaceId, Integer.MAX_VALUE, null)
            .content()
            .forEach(channel -> {
                channel.delete();
                channelRepository.save(channel);
            });
        workspaceMemberRepository.deleteAllByWorkspaceId(workspaceId);

        workspace.delete();
        workspaceRepository.save(workspace);
    }

    public List<WorkspaceMemberResponse> getMembers(String cognitoSub, String workspaceId) {
        User user = getUser(cognitoSub);
        getWorkspaceOrThrow(workspaceId);
        getMemberOrThrow(workspaceId, user.getId());
        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspaceId);
        List<String> userIds = members.stream().map(WorkspaceMember::getUserId).toList();
        Map<String, User> userMap = userRepository.findAllByIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));
        return members.stream()
            .map(m -> {
                User u = userMap.get(m.getUserId());
                if (u == null) return null;
                return WorkspaceMemberResponse.from(m, u);
            })
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

        workspaceMemberRepository.save(
            WorkspaceMember.createPending(workspaceId, request.userId(), WorkspaceRole.MEMBER));
    }

    public void acceptInvitation(String cognitoSub, String workspaceId) {
        User user = getUser(cognitoSub);
        getWorkspaceOrThrow(workspaceId);
        WorkspaceMember member = workspaceMemberRepository
            .findByWorkspaceIdAndUserId(workspaceId, user.getId())
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.WORKSPACE_INVITATION_NOT_FOUND));

        if (!member.isPending()) {
            throw new BusinessException(BusinessErrorCode.WORKSPACE_MEMBER_ALREADY_EXISTS);
        }
        member.activate();
        workspaceMemberRepository.save(member);
    }

    public void changeRole(String cognitoSub, String workspaceId, ChangeRoleRequest request) {
        User currentUser = getUser(cognitoSub);
        getWorkspaceOrThrow(workspaceId);
        WorkspaceMember currentMember = getMemberOrThrow(workspaceId, currentUser.getId());
        requireAdmin(currentMember);

        WorkspaceMember targetMember = getMemberOrThrow(workspaceId, request.userId());
        if (request.role() != WorkspaceRole.ADMIN && targetMember.isAdmin()) {
            long adminCount = workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
                .filter(WorkspaceMember::isAdmin)
                .count();
            if (adminCount <= 1) {
                throw new BusinessException(BusinessErrorCode.LAST_WORKSPACE_ADMIN);
            }
        }
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
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.WORKSPACE_MEMBER_NOT_FOUND));
        if (member.isPending()) {
            throw new BusinessException(BusinessErrorCode.WORKSPACE_MEMBER_NOT_FOUND);
        }
        return member;
    }

    private void requireAdmin(WorkspaceMember member) {
        if (!member.isAdmin()) {
            throw new BusinessException(BusinessErrorCode.NOT_WORKSPACE_ADMIN);
        }
    }
}
