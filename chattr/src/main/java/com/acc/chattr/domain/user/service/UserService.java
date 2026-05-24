package com.acc.chattr.domain.user.service;

import com.acc.chattr.common.code.BusinessErrorCode;
import com.acc.chattr.common.exception.BusinessException;
import com.acc.chattr.domain.user.dto.UserResponse;
import com.acc.chattr.domain.user.entity.User;
import com.acc.chattr.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
