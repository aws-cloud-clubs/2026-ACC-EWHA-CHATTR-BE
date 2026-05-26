package com.acc.chattr.domain.user.service;

import com.acc.chattr.common.code.BusinessErrorCode;
import com.acc.chattr.common.exception.BusinessException;
import com.acc.chattr.common.response.PageResponse;
import com.acc.chattr.domain.user.dto.UserResponse;
import com.acc.chattr.domain.user.entity.User;
import com.acc.chattr.domain.user.repository.UserRepository;
import com.acc.chattr.domain.workspace.repository.WorkspaceMemberRepository;
import com.acc.chattr.domain.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public UserService(UserRepository userRepository,
                       WorkspaceRepository workspaceRepository,
                       WorkspaceMemberRepository workspaceMemberRepository) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public UserResponse getMe(String cognitoSub) {
        User user = userRepository.findByCognitoSub(cognitoSub)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        return PageResponse.of(userRepository.findAll(), page, size, UserResponse::from);
    }

    public PageResponse<UserResponse> searchUsers(String query, int page, int size) {
        return PageResponse.of(userRepository.findByQuery(query), page, size, UserResponse::from);
    }

    public PageResponse<UserResponse> getOnlineUsers(int page, int size) {
        return PageResponse.of(userRepository.findOnlineUsers(), page, size, UserResponse::from);
    }

    public PageResponse<UserResponse> getWorkspaceUsers(String cognitoSub, String workspaceId, String query, int page, int size) {
        User requester = userRepository.findByCognitoSub(cognitoSub)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND));
        workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.WORKSPACE_NOT_FOUND));
        workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, requester.getId())
            .filter(m -> !m.isPending())
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.WORKSPACE_MEMBER_NOT_FOUND));
        List<String> userIds = workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
            .map(m -> m.getUserId())
            .toList();
        List<User> filtered = userRepository.findAllByIds(userIds).stream()
            .filter(u -> query == null || query.isBlank()
                || u.getEmail().contains(query)
                || u.getNickname().contains(query))
            .toList();
        return PageResponse.of(filtered, page, size, UserResponse::from);
    }
}
