package com.acc.chattr.domain.channel.repository;

import com.acc.chattr.domain.channel.entity.ChannelMember;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Slf4j
@Repository
public class ChannelMemberDynamoRepository implements ChannelMemberRepository {

    private static final int BATCH_SIZE = 25;
    private static final int MAX_RETRIES = 3;

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<ChannelMember> table;
    private final DynamoDbIndex<ChannelMember> userChannelsIndex;

    public ChannelMemberDynamoRepository(DynamoDbEnhancedClient enhancedClient,
                                         DynamoDbTable<ChannelMember> channelMemberTable) {
        this.enhancedClient = enhancedClient;
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

    @Override
    public void deleteAllByChannelId(String channelId) {
        List<ChannelMember> items = table.query(
                QueryConditional.keyEqualTo(Key.builder().partitionValue(channelId).build()))
            .stream()
            .flatMap(page -> page.items().stream())
            .toList();
        batchDelete(items);
    }

    private void batchDelete(List<ChannelMember> items) {
        for (int i = 0; i < items.size(); i += BATCH_SIZE) {
            List<ChannelMember> batch = items.subList(i, Math.min(i + BATCH_SIZE, items.size()));
            WriteBatch.Builder<ChannelMember> batchBuilder = WriteBatch.builder(ChannelMember.class)
                .mappedTableResource(table);
            batch.forEach(batchBuilder::addDeleteItem);
            BatchWriteResult result = enhancedClient.batchWriteItem(
                BatchWriteItemEnhancedRequest.builder().writeBatches(batchBuilder.build()).build());
            List<Key> unprocessed = result.unprocessedDeleteItemsForTable(table);
            int retries = 0;
            while (!unprocessed.isEmpty() && retries < MAX_RETRIES) {
                try {
                    Thread.sleep(100L << retries); // 100ms, 200ms, 400ms
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                WriteBatch.Builder<ChannelMember> retryBuilder = WriteBatch.builder(ChannelMember.class)
                    .mappedTableResource(table);
                unprocessed.forEach(retryBuilder::addDeleteItem);
                result = enhancedClient.batchWriteItem(
                    BatchWriteItemEnhancedRequest.builder().writeBatches(retryBuilder.build()).build());
                unprocessed = result.unprocessedDeleteItemsForTable(table);
                retries++;
            }
            if (!unprocessed.isEmpty()) {
                log.warn("batchDelete: {} unprocessed channel-member items remaining after {} retries",
                    unprocessed.size(), MAX_RETRIES);
            }
        }
    }
}
