package com.acc.chattr.domain.user.repository;

import com.acc.chattr.domain.user.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(String userId);
    Optional<User> findByCognitoSub(String cognitoSub);
    List<User> findAllByIds(List<String> userIds);
    List<User> findAll();
    List<User> findByQuery(String query);
    List<User> findOnlineUsers();
}
