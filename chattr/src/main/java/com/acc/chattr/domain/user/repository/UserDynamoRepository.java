package com.acc.chattr.domain.user.repository;

import com.acc.chattr.common.response.CursorPageResponse;
import com.acc.chattr.common.util.CursorUtils;
import com.acc.chattr.domain.user.entity.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserDynamoRepository implements UserRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<User> table;
    private final DynamoDbIndex<User> cognitoSubIndex;

    public UserDynamoRepository(DynamoDbEnhancedClient enhancedClient, DynamoDbTable<User> userTable) {
        this.enhancedClient = enhancedClient;
        this.table = userTable;
        this.cognitoSubIndex = userTable.index("cognito-sub-index");
    }

    @Override
    public void save(User user) {
        table.putItem(user);
    }

    @Override
    public Optional<User> findById(String userId) {
        User user = table.getItem(Key.builder().partitionValue(userId).build());
        if (user == null || user.isDeleted()) return Optional.empty();
        return Optional.of(user);
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
    public List<User> findAllByIds(List<String> userIds) {
        if (userIds.isEmpty()) return List.of();
        List<User> result = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i += 100) {
            List<String> chunk = userIds.subList(i, Math.min(i + 100, userIds.size()));
            ReadBatch.Builder<User> batchBuilder = ReadBatch.builder(User.class).mappedTableResource(table);
            chunk.forEach(id -> batchBuilder.addGetItem(Key.builder().partitionValue(id).build()));
            enhancedClient.batchGetItem(BatchGetItemEnhancedRequest.builder()
                    .readBatches(batchBuilder.build()).build())
                .stream()
                .flatMap(page -> page.resultsForTable(table).stream())
                .filter(u -> !u.isDeleted())
                .forEach(result::add);
        }
        return result;
    }

    @Override
    public CursorPageResponse<User> findAll(int size, String cursor) {
        Expression filter = Expression.builder()
            .expression("attribute_not_exists(deletedAt)")
            .build();
        return scanPage(filter, size, cursor);
    }

    @Override
    public CursorPageResponse<User> findByQuery(String query, int size, String cursor) {
        Expression filter = Expression.builder()
            .expression("attribute_not_exists(deletedAt) AND (contains(#email, :q) OR contains(#nickname, :q))")
            .expressionNames(Map.of("#email", "email", "#nickname", "nickname"))
            .expressionValues(Map.of(":q", AttributeValue.fromS(query)))
            .build();
        return scanPage(filter, size, cursor);
    }

    @Override
    public CursorPageResponse<User> findOnlineUsers(int size, String cursor) {
        Expression filter = Expression.builder()
            .expression("attribute_not_exists(deletedAt) AND #online = :online")
            .expressionNames(Map.of("#online", "online"))
            .expressionValues(Map.of(":online", AttributeValue.fromBool(true)))
            .build();
        return scanPage(filter, size, cursor);
    }

    // ─── 내부 헬퍼 ─────────────────────────────────────────────────────────────

    /**
     * DynamoDB Scan + FilterExpression 기반 커서 페이징.
     *
     * <p>DynamoDB의 {@code limit}은 필터 적용 전 스캔 항목 수를 제한합니다.
     * 따라서 필터 조건이 있을 때는 {@code size}개보다 적게 반환될 수 있습니다.
     * 이는 DynamoDB 커서 페이징의 알려진 특성입니다.</p>
     */
    private CursorPageResponse<User> scanPage(Expression filter, int size,
                                               String cursor) {
        Map<String, AttributeValue> startKey = CursorUtils.decode(cursor);

        ScanEnhancedRequest.Builder builder = ScanEnhancedRequest.builder()
            .filterExpression(filter)
            .limit(size);
        if (startKey != null) builder.exclusiveStartKey(startKey);

        Page<User> page = table.scan(builder.build()).iterator().next();
        String nextCursor = CursorUtils.encode(page.lastEvaluatedKey());
        return CursorPageResponse.of(page.items(), nextCursor);
    }
}
