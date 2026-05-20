package com.acc.chattr.domain.message;

import com.acc.chattr.domain.common.BaseEntity;
import com.acc.chattr.infrastructure.dynamodb.converter.RoomTypeConverter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
public class Message extends BaseEntity {

    @Getter(AccessLevel.NONE)
    private String id;

    @Getter(AccessLevel.NONE)
    private String roomId;

    @Getter(AccessLevel.NONE)
    private RoomType roomType;

    private String senderId;
    private String parentMessageId;
    private String content;
    private Instant editedAt;
    private List<MessageAttachment> attachments = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    private Long ttl;

    private Message(
            String id,
            String senderId,
            String parentMessageId,
            String content,
            Long ttl,
            List<MessageAttachment> attachments,
            String roomId,
            RoomType roomType
    ) {
        this.id = id;
        this.senderId = senderId;
        this.parentMessageId = parentMessageId;
        this.content = content;
        this.ttl = ttl;
        this.attachments = attachments == null ? new ArrayList<>() : new ArrayList<>(attachments);
        this.editedAt = Instant.now();
        this.roomId = roomId;
        this.roomType = roomType;
        initCreatedAt();
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"room-messages-index"})
    public String getRoomId() {
        return roomId;
    }

    @Override
    @DynamoDbSecondarySortKey(indexNames = {"room-messages-index"})
    public Instant getCreatedAt() {
        return super.getCreatedAt();
    }

    @DynamoDbConvertedBy(RoomTypeConverter.class)
    public RoomType getRoomType() {
        return roomType;
    }

    @DynamoDbAttribute("ttl")
    public Long getTtl() {
        return ttl;
    }

    public static Message createChannelMessage(String id, String senderId, String channelId, String content) {
        return new Message(id, senderId, null, content, null, null, channelId, RoomType.CHANNEL);
    }

    public static Message createDmMessage(String id, String senderId, String dmId, String content) {
        return new Message(id, senderId, null, content, null, null, dmId, RoomType.DM);
    }

    public Message reply(String id, String senderId, String content) {
        return new Message(id, senderId, this.id, content, null, null, this.roomId, this.roomType);
    }

    public void edit(String content) {
        this.content = content;
        this.editedAt = Instant.now();
    }

    public void expireAfter(Long ttl) {
        this.ttl = ttl;
    }
}
