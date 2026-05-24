package com.acc.chattr.domain.auth.entity;

import com.acc.chattr.domain.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class Device extends BaseEntity {

    @Getter(AccessLevel.NONE)
    private String userId;

    @Getter(AccessLevel.NONE)
    private String deviceId;

    private String deviceName;
    private String platform;
    private Instant lastActiveAt;

    private Device(String userId, String deviceId, String deviceName, String platform) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.platform = platform;
        this.lastActiveAt = Instant.now();
        initCreatedAt();
    }

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    @DynamoDbSortKey
    public String getDeviceId() {
        return deviceId;
    }

    public static Device create(String userId, String deviceId, String deviceName, String platform) {
        return new Device(userId, deviceId, deviceName, platform);
    }

    public void refreshActivity() {
        this.lastActiveAt = Instant.now();
    }
}
