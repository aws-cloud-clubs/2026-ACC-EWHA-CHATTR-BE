package com.acc.chattr.domain.user;

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
public class User extends BaseEntity {

    @Getter(AccessLevel.NONE)
    private String id;

    @Getter(AccessLevel.NONE)
    private String cognitoSub;

    private String email;
    private String nickname;
    private boolean online;

    private User(String id, String email, String nickname, String cognitoSub) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.cognitoSub = cognitoSub;
        this.online = false;
        initCreatedAt();
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"cognito-sub-index"})
    public String getCognitoSub() {
        return cognitoSub;
    }

    public static User create(String id, String email, String nickname, String cognitoSub) {
        return new User(id, email, nickname, cognitoSub);
    }

    public void updateProfile(String nickname) {
        this.nickname = nickname;
    }

    public void markOnline() {
        this.online = true;
    }

    public void markOffline() {
        this.online = false;
    }
}
