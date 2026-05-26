package com.acc.chattr.domain.channel.repository;

import com.acc.chattr.common.response.CursorPageResponse;
import com.acc.chattr.common.util.CursorUtils;
import com.acc.chattr.domain.channel.entity.Channel;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class ChannelDynamoRepository implements ChannelRepository {

    private final DynamoDbTable<Channel> table;
    private final DynamoDbIndex<Channel> workspaceChannelsIndex;

    public ChannelDynamoRepository(DynamoDbTable<Channel> channelTable) {
        this.table = channelTable;
        this.workspaceChannelsIndex = channelTable.index("workspace-channels-index");
    }

    @Override
    public void save(Channel channel) {
        table.putItem(channel);
    }

    @Override
    public Optional<Channel> findById(String channelId) {
        Channel channel = table.getItem(Key.builder().partitionValue(channelId).build());
        if (channel == null || channel.isDeleted()) return Optional.empty();
        return Optional.of(channel);
    }

    @Override
    public CursorPageResponse<Channel> findByWorkspaceId(String workspaceId, int size, String cursor) {
        Map<String, AttributeValue> startKey = CursorUtils.decode(cursor);

        // 삭제되지 않은 채널만 조회
        Expression notDeleted = Expression.builder()
            .expression("attribute_not_exists(deletedAt)")
            .build();

        QueryEnhancedRequest.Builder builder = QueryEnhancedRequest.builder()
            .queryConditional(QueryConditional.keyEqualTo(
                Key.builder().partitionValue(workspaceId).build()))
            .filterExpression(notDeleted)
            .limit(size);
        if (startKey != null) builder.exclusiveStartKey(startKey);

        Page<Channel> page = workspaceChannelsIndex.query(builder.build()).iterator().next();
        String nextCursor = CursorUtils.encode(page.lastEvaluatedKey());
        return CursorPageResponse.of(page.items(), nextCursor);
    }

    @Override
    public List<String> findAllIdsByWorkspaceId(String workspaceId) {
        return StreamSupport.stream(
            workspaceChannelsIndex.query(QueryConditional.keyEqualTo(
                Key.builder().partitionValue(workspaceId).build())).spliterator(), false)
            .flatMap(page -> page.items().stream())
            .map(Channel::getId)
            .toList();
    }
}
