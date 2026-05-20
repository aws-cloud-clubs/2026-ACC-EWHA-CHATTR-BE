package com.acc.chattr.domain.dm.entity;

import com.acc.chattr.domain.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class Dm extends BaseEntity {

    @Getter(AccessLevel.NONE)
    private String id;

    @Getter(AccessLevel.NONE)
    private String userAId;

    @Getter(AccessLevel.NONE)
    private String userBId;

    private Dm(String id, String userAId, String userBId) {
        this.id = id;
        this.userAId = userAId;
        this.userBId = userBId;
        initCreatedAt();
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"dm-users-index"})
    public String getUserAId() {
        return userAId;
    }

    @DynamoDbSecondarySortKey(indexNames = {"dm-users-index"})
    public String getUserBId() {
        return userBId;
    }

    // Normalized so that userAId < userBId — (A,B) and (B,A) always map to the same record
    public static Dm create(String id, String userId1, String userId2) {
        if (userId1.compareTo(userId2) <= 0) {
            return new Dm(id, userId1, userId2);
        }
        return new Dm(id, userId2, userId1);
    }

    public boolean hasParticipant(String userId) {
        return userAId.equals(userId) || userBId.equals(userId);
    }
}
