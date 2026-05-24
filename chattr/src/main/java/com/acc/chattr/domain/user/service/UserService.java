package com.acc.chattr.domain.user.service;

import com.acc.chattr.common.code.BusinessErrorCode;
import com.acc.chattr.common.exception.BusinessException;
import com.acc.chattr.domain.user.dto.UserResponse;
import com.acc.chattr.domain.user.entity.User;
import com.acc.chattr.domain.user.repository.UserRepository;
import com.acc.chattr.domain.workspace.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public UserService(UserRepository userRepository, WorkspaceMemberRepository workspaceMemberRepository) {
        this.userRepository = userRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public UserResponse getMe(String cognitoSub) {
        User user = userRepository.findByCognitoSub(cognitoSub)
            .orElseThrow(() -> new BusinessException(BusinessErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserResponse::from)
            .toList();
    }

    public List<UserResponse> searchUsers(String query) {
        return userRepository.findByQuery(query).stream()
            .map(UserResponse::from)
            .toList();
    }

    public List<UserResponse> getOnlineUsers() {
        return userRepository.findOnlineUsers().stream()
            .map(UserResponse::from)
            .toList();
    }

    public List<UserResponse> getWorkspaceUsers(String workspaceId, String query) {
        return workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
            .map(m -> userRepository.findById(m.getUserId()).orElse(null))
            .filter(Objects::nonNull)
            .filter(u -> query == null || query.isBlank()
                || u.getEmail().contains(query)
                || u.getNickname().contains(query))
            .map(UserResponse::from)
            .toList();
    }
}
