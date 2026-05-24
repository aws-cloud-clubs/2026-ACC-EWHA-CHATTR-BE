package com.acc.chattr.domain.channel.repository;

import com.acc.chattr.domain.channel.entity.ChannelMember;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class ChannelMemberDynamoRepository implements ChannelMemberRepository {

    private final DynamoDbTable<ChannelMember> table;
    private final DynamoDbIndex<ChannelMember> userChannelsIndex;

    public ChannelMemberDynamoRepository(DynamoDbTable<ChannelMember> channelMemberTable) {
        this.table = channelMemberTable;
        this.userChannelsIndex = channelMemberTable.index("user-channels-index");
    }

    @Override
    public void save(ChannelMember member) {
        table.putItem(member);
    }

    @Override
    public Optional<ChannelMember> findByChannelIdAndUserId(String channelId, String userId) {
        ChannelMember member = table.getItem(Key.builder()
            .partitionValue(channelId)
            .sortValue(userId)
            .build());
        if (member == null || member.isDeleted()) return Optional.empty();
        return Optional.of(member);
    }

    @Override
    public List<ChannelMember> findByChannelId(String channelId) {
        return table.query(QueryConditional.keyEqualTo(Key.builder().partitionValue(channelId).build()))
            .stream()
            .flatMap(page -> page.items().stream())
            .filter(m -> !m.isDeleted())
            .toList();
    }

    @Override
    public List<ChannelMember> findByUserId(String userId) {
        return StreamSupport.stream(userChannelsIndex
            .query(QueryConditional.keyEqualTo(Key.builder().partitionValue(userId).build()))
            .spliterator(), false)
            .flatMap(page -> page.items().stream())
            .filter(m -> !m.isDeleted())
            .toList();
    }
}
