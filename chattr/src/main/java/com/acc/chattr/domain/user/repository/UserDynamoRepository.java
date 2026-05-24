package com.acc.chattr.domain.user.repository;

import com.acc.chattr.domain.user.entity.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    @Override
    public List<User> findAll() {
        Expression filter = Expression.builder()
            .expression("attribute_not_exists(deletedAt)")
            .build();

        return StreamSupport.stream(
            table.scan(ScanEnhancedRequest.builder().filterExpression(filter).build()).spliterator(), false)
            .flatMap(page -> page.items().stream())
            .collect(Collectors.toList());
    }

    @Override
    public List<User> findByQuery(String query) {
        Expression filter = Expression.builder()
            .expression("attribute_not_exists(deletedAt) AND (contains(#email, :q) OR contains(#nickname, :q))")
            .expressionNames(Map.of("#email", "email", "#nickname", "nickname"))
            .expressionValues(Map.of(":q", AttributeValue.fromS(query)))
            .build();

        return StreamSupport.stream(
            table.scan(ScanEnhancedRequest.builder().filterExpression(filter).build()).spliterator(), false)
            .flatMap(page -> page.items().stream())
            .collect(Collectors.toList());
    }

    @Override
    public List<User> findOnlineUsers() {
        Expression filter = Expression.builder()
            .expression("attribute_not_exists(deletedAt) AND #online = :online")
            .expressionNames(Map.of("#online", "online"))
            .expressionValues(Map.of(":online", AttributeValue.fromBool(true)))
            .build();

        return StreamSupport.stream(
            table.scan(ScanEnhancedRequest.builder().filterExpression(filter).build()).spliterator(), false)
            .flatMap(page -> page.items().stream())
            .collect(Collectors.toList());
    }
}
