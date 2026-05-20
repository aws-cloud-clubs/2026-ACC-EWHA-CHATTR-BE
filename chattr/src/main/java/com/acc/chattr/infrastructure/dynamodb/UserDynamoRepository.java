package com.acc.chattr.infrastructure.dynamodb;

import com.acc.chattr.domain.user.User;
import com.acc.chattr.domain.user.UserRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.Optional;

@Repository
public class UserDynamoRepository implements UserRepository {
    private final DynamoDbTable<User> table;
    private final DynamoDbIndex<User> cognitoSubIndex;

    public UserDynamoRepository(DynamoDbTable<User> userTable) {
        this.table = userTable;
        this.cognitoSubIndex = userTable.index("cognito-sub-index");
    }

    @Override
    public void save(User user) {
        table.putItem(user);
    }

    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(userId).build()));
    }

    @Override
    public Optional<User> findByCognitoSub(String cognitoSub) {
        return cognitoSubIndex
            .query(QueryConditional.keyEqualTo(Key.builder().partitionValue(cognitoSub).build()))
            .stream()
            .flatMap(page -> page.items().stream())
            .filter(u -> !u.isDeleted())
            .findFirst();
    }
}
