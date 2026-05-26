package com.acc.chattr.domain.workspace.repository;

import com.acc.chattr.domain.workspace.entity.WorkspaceMember;
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

@Repository
public class WorkspaceMemberDynamoRepository implements WorkspaceMemberRepository {

    private static final int BATCH_SIZE = 25;

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<WorkspaceMember> table;
    private final DynamoDbIndex<WorkspaceMember> userWorkspacesIndex;

    public WorkspaceMemberDynamoRepository(DynamoDbEnhancedClient enhancedClient,
                                           DynamoDbTable<WorkspaceMember> workspaceMemberTable) {
        this.enhancedClient = enhancedClient;
        this.table = workspaceMemberTable;
        this.userWorkspacesIndex = workspaceMemberTable.index("user-workspaces-index");
    }

    @Override
    public void save(WorkspaceMember member) {
        table.putItem(member);
    }

    @Override
    public Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId) {
        WorkspaceMember member = table.getItem(Key.builder()
            .partitionValue(workspaceId)
            .sortValue(userId)
            .build());
        if (member == null || member.isDeleted()) return Optional.empty();
        return Optional.of(member);
    }

    @Override
    public List<WorkspaceMember> findByWorkspaceId(String workspaceId) {
        return table.query(QueryConditional.keyEqualTo(Key.builder().partitionValue(workspaceId).build()))
            .stream()
            .flatMap(page -> page.items().stream())
            .filter(m -> !m.isDeleted() && !m.isPending())
            .toList();
    }

    @Override
    public List<WorkspaceMember> findByUserId(String userId) {
        return StreamSupport.stream(userWorkspacesIndex
            .query(QueryConditional.keyEqualTo(Key.builder().partitionValue(userId).build()))
            .spliterator(), false)
            .flatMap(page -> page.items().stream())
            .filter(m -> !m.isDeleted() && !m.isPending())
            .toList();
    }

    @Override
    public void deleteAllByWorkspaceId(String workspaceId) {
        List<WorkspaceMember> items = table.query(
                QueryConditional.keyEqualTo(Key.builder().partitionValue(workspaceId).build()))
            .stream()
            .flatMap(page -> page.items().stream())
            .toList();
        batchDelete(items);
    }

    private void batchDelete(List<WorkspaceMember> items) {
        for (int i = 0; i < items.size(); i += BATCH_SIZE) {
            List<WorkspaceMember> batch = items.subList(i, Math.min(i + BATCH_SIZE, items.size()));
            WriteBatch.Builder<WorkspaceMember> batchBuilder = WriteBatch.builder(WorkspaceMember.class)
                .mappedTableResource(table);
            batch.forEach(batchBuilder::addDeleteItem);
            BatchWriteResult result = enhancedClient.batchWriteItem(
                BatchWriteItemEnhancedRequest.builder().writeBatches(batchBuilder.build()).build());
            List<Key> unprocessed = result.unprocessedDeleteItemsForTable(table);
            while (!unprocessed.isEmpty()) {
                WriteBatch.Builder<WorkspaceMember> retryBuilder = WriteBatch.builder(WorkspaceMember.class)
                    .mappedTableResource(table);
                unprocessed.forEach(retryBuilder::addDeleteItem);
                result = enhancedClient.batchWriteItem(
                    BatchWriteItemEnhancedRequest.builder().writeBatches(retryBuilder.build()).build());
                unprocessed = result.unprocessedDeleteItemsForTable(table);
            }
        }
    }
}
