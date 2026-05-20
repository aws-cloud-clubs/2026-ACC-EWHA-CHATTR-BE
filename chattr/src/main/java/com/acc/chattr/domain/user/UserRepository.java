package com.acc.chattr.domain.user;

import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(String userId);
    Optional<User> findByCognitoSub(String cognitoSub);
}
