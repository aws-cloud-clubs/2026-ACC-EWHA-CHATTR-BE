package com.acc.chattr.domain.channel;

import com.acc.chattr.domain.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class ChannelMember extends BaseEntity {

    @Getter(AccessLevel.NONE)
    private String channelId;

    @Getter(AccessLevel.NONE)
    private String userId;

    private ChannelMember(String channelId, String userId) {
        this.channelId = channelId;
        this.userId = userId;
        initCreatedAt();
    }

    @DynamoDbPartitionKey
    @DynamoDbSecondarySortKey(indexNames = {"user-channels-index"})
    public String getChannelId() {
        return channelId;
    }

    @DynamoDbSortKey
    @DynamoDbSecondaryPartitionKey(indexNames = {"user-channels-index"})
    public String getUserId() {
        return userId;
    }

    public static ChannelMember create(String channelId, String userId) {
        return new ChannelMember(channelId, userId);
    }
}
