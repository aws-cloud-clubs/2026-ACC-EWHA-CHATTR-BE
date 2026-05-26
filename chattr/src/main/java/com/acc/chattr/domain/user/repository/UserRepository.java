package com.acc.chattr.domain.user.repository;

import com.acc.chattr.common.response.CursorPageResponse;
import com.acc.chattr.domain.user.entity.User;
import com.acc.chattr.domain.user.dto.UserResponse;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(String userId);
    Optional<User> findByCognitoSub(String cognitoSub);
    List<User> findAllByIds(List<String> userIds);

    /** 전체 유저 커서 페이징 (DynamoDB Scan + LastEvaluatedKey) */
    CursorPageResponse<User> findAll(int size, String cursor);

    /** 이메일/닉네임 검색 커서 페이징 (DynamoDB FilterExpression) */
    CursorPageResponse<User> findByQuery(String query, int size, String cursor);

    /** 온라인 유저 커서 페이징 (DynamoDB FilterExpression) */
    CursorPageResponse<User> findOnlineUsers(int size, String cursor);
}
