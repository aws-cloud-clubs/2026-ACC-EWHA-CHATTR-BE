package com.acc.chattr.domain.workspace.repository;

import com.acc.chattr.domain.workspace.entity.Workspace;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

@Repository
public class WorkspaceDynamoRepository implements WorkspaceRepository {

    private final DynamoDbTable<Workspace> table;

    public WorkspaceDynamoRepository(DynamoDbTable<Workspace> workspaceTable) {
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
}
