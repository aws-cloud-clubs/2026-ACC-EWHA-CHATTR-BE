package com.acc.chattr.domain.workspace.entity;

import com.acc.chattr.domain.common.BaseEntity;
import com.acc.chattr.domain.workspace.repository.WorkspaceRoleConverter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class WorkspaceMember extends BaseEntity {

    @Getter(AccessLevel.NONE)
    private String workspaceId;

    @Getter(AccessLevel.NONE)
    private String userId;

    @Getter(AccessLevel.NONE)
    private WorkspaceRole role;

    private String field;

    private WorkspaceMember(String workspaceId, String userId, WorkspaceRole role) {
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.role = role;
        initCreatedAt();
    }

    @DynamoDbPartitionKey
    @DynamoDbSecondarySortKey(indexNames = {"user-workspaces-index"})
    public String getWorkspaceId() {
        return workspaceId;
    }

    @DynamoDbSortKey
    @DynamoDbSecondaryPartitionKey(indexNames = {"user-workspaces-index"})
    public String getUserId() {
        return userId;
    }

    @DynamoDbConvertedBy(WorkspaceRoleConverter.class)
    public WorkspaceRole getRole() {
        return role;
    }

    public static WorkspaceMember create(String workspaceId, String userId, WorkspaceRole role) {
        return new WorkspaceMember(workspaceId, userId, role);
    }

    public void changeRole(WorkspaceRole role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return role == WorkspaceRole.ADMIN;
    }
}
