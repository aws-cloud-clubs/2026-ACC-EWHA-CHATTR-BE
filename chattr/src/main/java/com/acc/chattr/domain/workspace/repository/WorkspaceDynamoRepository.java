package com.acc.chattr.domain.workspace.repository;

import com.acc.chattr.domain.workspace.entity.Workspace;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkspaceDynamoRepository implements WorkspaceRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Workspace> table;

    public WorkspaceDynamoRepository(DynamoDbEnhancedClient enhancedClient, DynamoDbTable<Workspace> workspaceTable) {
        this.enhancedClient = enhancedClient;
        this.table = workspaceTable;
    }

    @Override
    public void save(Workspace workspace) {
        table.putItem(workspace);
    }

    @Override
    public Optional<Workspace> findById(String workspaceId) {
        Workspace workspace = table.getItem(Key.builder().partitionValue(workspaceId).build());
        if (workspace == null || workspace.isDeleted()) return Optional.empty();
        return Optional.of(workspace);
    }

    @Override
    public List<Workspace> findAllByIds(List<String> workspaceIds) {
        if (workspaceIds.isEmpty()) return List.of();
        List<Workspace> result = new ArrayList<>();
        for (int i = 0; i < workspaceIds.size(); i += 100) {
            List<String> chunk = workspaceIds.subList(i, Math.min(i + 100, workspaceIds.size()));
            ReadBatch.Builder<Workspace> batchBuilder = ReadBatch.builder(Workspace.class).mappedTableResource(table);
            chunk.forEach(id -> batchBuilder.addGetItem(Key.builder().partitionValue(id).build()));
            BatchGetItemEnhancedRequest batchRequest = BatchGetItemEnhancedRequest.builder()
                .readBatches(batchBuilder.build())
                .build();
            enhancedClient.batchGetItem(batchRequest)
                .stream()
                .flatMap(page -> page.resultsForTable(table).stream())
                .filter(w -> !w.isDeleted())
                .forEach(result::add);
        }
        return result;
    }
}
