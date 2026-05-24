package com.acc.chattr.domain.workspace.repository;

import com.acc.chattr.domain.workspace.entity.WorkspaceMember;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class WorkspaceMemberDynamoRepository implements WorkspaceMemberRepository {

    private final DynamoDbTable<WorkspaceMember> table;
    private final DynamoDbIndex<WorkspaceMember> userWorkspacesIndex;

    public WorkspaceMemberDynamoRepository(DynamoDbTable<WorkspaceMember> workspaceMemberTable) {
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
            .filter(m -> !m.isDeleted())
            .toList();
    }

    @Override
    public List<WorkspaceMember> findByUserId(String userId) {
        return StreamSupport.stream(userWorkspacesIndex
            .query(QueryConditional.keyEqualTo(Key.builder().partitionValue(userId).build()))
            .spliterator(), false)
            .flatMap(page -> page.items().stream())
            .filter(m -> !m.isDeleted())
            .toList();
    }
}
