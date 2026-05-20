package com.acc.chattr.domain.workspace.entity;

import com.acc.chattr.domain.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class Workspace extends BaseEntity {

    @Getter(AccessLevel.NONE)
    private String id;

    private String name;

    private Workspace(String id, String name) {
        this.id = id;
        this.name = name;
        initCreatedAt();
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public static Workspace create(String id, String name) {
        return new Workspace(id, name);
    }

    public void rename(String name) {
        this.name = name;
    }
}
