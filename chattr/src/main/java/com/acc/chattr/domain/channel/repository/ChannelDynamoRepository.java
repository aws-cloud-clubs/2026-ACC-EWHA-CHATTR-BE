package com.acc.chattr.domain.channel.repository;

import com.acc.chattr.domain.channel.entity.Channel;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
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
    public List<Channel> findByWorkspaceId(String workspaceId) {
        return StreamSupport.stream(
            workspaceChannelsIndex.query(QueryConditional.keyEqualTo(
                Key.builder().partitionValue(workspaceId).build())).spliterator(), false)
            .flatMap(page -> page.items().stream())
            .filter(c -> !c.isDeleted())
            .toList();
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
