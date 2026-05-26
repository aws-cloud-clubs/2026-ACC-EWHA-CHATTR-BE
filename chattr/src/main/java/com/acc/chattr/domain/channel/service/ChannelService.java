package com.acc.chattr.domain.channel.service;

import com.acc.chattr.common.code.BusinessErrorCode;
import com.acc.chattr.common.exception.BusinessException;
import com.acc.chattr.common.response.PageResponse;
import com.acc.chattr.domain.channel.dto.AddMemberRequest;
import com.acc.chattr.domain.channel.dto.ChannelCreateRequest;
import com.acc.chattr.domain.channel.dto.ChannelMemberResponse;
import com.acc.chattr.domain.channel.dto.ChannelResponse;
import com.acc.chattr.domain.channel.dto.ChannelUpdateRequest;
import com.acc.chattr.domain.channel.entity.Channel;
import com.acc.chattr.domain.channel.entity.ChannelMember;
import com.acc.chattr.domain.channel.repository.ChannelMemberRepository;
import com.acc.chattr.domain.channel.repository.ChannelRepository;
import com.acc.chattr.domain.user.entity.User;
import com.acc.chattr.domain.user.repository.UserRepository;
import com.acc.chattr.domain.workspace.entity.WorkspaceMember;
import com.acc.chattr.domain.workspace.repository.WorkspaceMemberRepository;
import com.acc.chattr.domain.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    public ChannelService(ChannelRepository channelRepository,
                          ChannelMemberRepository channelMemberRepository,
                          WorkspaceRepository workspaceRepository,
                          WorkspaceMemberRepository workspaceMemberRepository,
                          UserRepository userRepository) {
        this.channelRepository = channelRepository;
        this.channelMemberRepository = channelMemberRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
    }

    public ChannelResponse create(String cognitoSub, String workspaceId, ChannelCreateRequest request) {
        User user = getUser(cognitoSub);
        requireWorkspaceExists(workspaceId);
        requireWorkspaceMember(workspaceId, user.getId());

        Channel channel = Channel.create(
            UUID.randomUUID().toString(),
            workspaceId,
            request.name(),
            request.description(),
            request.topic(),
            user.getId()
        );
        channelRepository.save(channel);
        channelMemberRepository.save(ChannelMember.create(channel.getId(), user.getId()));
        return ChannelResponse.from(channel);
    }

    public PageResponse<ChannelResponse> getChannels(String cognitoSub, String workspaceId, int page, int size) {
        User user = getUser(cognitoSub);
        requireWorkspaceExists(workspaceId);
        requireWorkspaceMember(workspaceId, user.getId());

        return PageResponse.of(channelRepository.findByWorkspaceId(workspaceId), page, size, ChannelResponse::from);
    }

    public ChannelResponse getChannel(String cognitoSub, String channelId) {
        User user = getUser(cognitoSub);
        Channel channel = getChannelOrThrow(channelId);
        requireWorkspaceMember(channel.getWorkspaceId(), user.getId());
        return ChannelResponse.from(channel);
    }

    public ChannelResponse update(String cognitoSub, String channelId, ChannelUpdateRequest request) {
        User user = getUser(cognitoSub);
        Channel channel = getChannelOrThrow(channelId);
        WorkspaceMember member = getActiveMemberOrThrow(channel.getWorkspaceId(), user.getId());
        requireChannelManager(channel, member);

        channel.updateInfo(request.name(), request.description(), request.topic());
        channelRepository.save(channel);
        return ChannelResponse.from(channel);
    }

    public void delete(String cognitoSub, String channelId) {
        User user = getUser(cognitoSub);
        Channel channel = getChannelOrThrow(channelId);
        WorkspaceMember member = getActiveMemberOrThrow(channel.getWorkspaceId(), user.getId());
        requireChannelManager(channel, member);

        channel.delete();
        channelRepository.save(channel);
    }

    public List<ChannelMemberResponse> getMembers(String cognitoSub, String channelId) {
        User user = getUser(cognitoSub);
        Channel channel = getChannelOrThrow(channelId);
        requireWorkspaceMember(channel.getWorkspaceId(), user.getId());

        List<ChannelMember> members = channelMemberRepository.findByChannelId(channelId);
        List<String> userIds = members.stream().map(ChannelMember::getUserId).toList();
        Map<String, User> userMap = userRepository.findAllByIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));
        return members.stream()
            .map(m -> {
                User u = userMap.get(m.getUserId());
                if (u == null) return null;
                return ChannelMemberResponse.from(m, u);
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public void addMember(String cognitoSub, String channelId, AddMemberRequest request) {
        User currentUser = getUser(cognitoSub);
        Channel channel = getChannelOrThrow(channelId);
        requireWorkspaceMember(channel.getWorkspaceId(), currentUser.getId());
        requireWorkspaceMember(channel.getWorkspaceId(), request.userId());

        if (channelMemberRepository.findByChannelIdAndUserId(channelId, request.userId()).isPresent()) {
            throw new BusinessException(BusinessErrorCode.CHANNEL_MEMBER_ALREADY_EXISTS);
        }
        channelMemberRepository.save(ChannelMember.create(channelId, request.userId()));
    }

    // ─── 내부 헬퍼 ─────────────────────────────────────────

    private User getUser(String cognitoSub) {
        return userRepository.findByCognitoSub(cognitoSub)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND));
    }

    private Channel getChannelOrThrow(String channelId) {
        return channelRepository.findById(channelId)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.CHANNEL_NOT_FOUND));
    }

    private void requireWorkspaceExists(String workspaceId) {
        workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.WORKSPACE_NOT_FOUND));
    }

    private WorkspaceMember getActiveMemberOrThrow(String workspaceId, String userId) {
        WorkspaceMember member = workspaceMemberRepository
            .findByWorkspaceIdAndUserId(workspaceId, userId)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.WORKSPACE_MEMBER_NOT_FOUND));
        if (member.isPending()) {
            throw new BusinessException(BusinessErrorCode.WORKSPACE_MEMBER_NOT_FOUND);
        }
        return member;
    }

    private void requireWorkspaceMember(String workspaceId, String userId) {
        getActiveMemberOrThrow(workspaceId, userId);
    }

    /** 채널 관리자 = 채널 생성자 OR 워크스페이스 ADMIN */
    private void requireChannelManager(Channel channel, WorkspaceMember workspaceMember) {
        boolean isCreator = channel.getCreatedById().equals(workspaceMember.getUserId());
        if (!isCreator && !workspaceMember.isAdmin()) {
            throw new BusinessException(BusinessErrorCode.NOT_CHANNEL_MANAGER);
        }
    }
}
