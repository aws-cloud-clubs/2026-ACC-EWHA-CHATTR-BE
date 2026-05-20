package com.acc.chattr.domain.channel;

import com.acc.chattr.domain.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class Channel extends BaseEntity {

    @Getter(AccessLevel.NONE)
    private String id;

    @Getter(AccessLevel.NONE)
    private String workspaceId;

    private String name;
    private String description;
    private String topic;
    private String createdById;

    private Channel(String id, String workspaceId, String name, String description, String topic, String createdById) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.topic = topic;
        this.createdById = createdById;
        initCreatedAt();
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"workspace-channels-index"})
    public String getWorkspaceId() {
        return workspaceId;
    }

    public static Channel create(String id, String workspaceId, String name, String description, String topic, String createdById) {
        return new Channel(id, workspaceId, name, description, topic, createdById);
    }

    public void updateInfo(String name, String description, String topic) {
        this.name = name;
        this.description = description;
        this.topic = topic;
    }
}
