package com.acc.chattr.domain.message;

import com.acc.chattr.domain.common.BaseEntity;
import com.acc.chattr.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseEntity {

    @Id
    @Column(name = "message_id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "parent_message_id", length = 36)
    private String parentMessageId;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "ttl")
    private Long ttl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachments", columnDefinition = "json")
    private List<MessageAttachment> attachments = new ArrayList<>();

    @Column(name = "edited_at", nullable = false)
    private LocalDateTime editedAt;

    @Column(name = "room_id", length = 36, nullable = false)
    private String roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    private Message(
            String messageId,
            User sender,
            String parentMessageId,
            String content,
            Long ttl,
            List<MessageAttachment> attachments,
            String roomId,
            RoomType roomType
    ) {
        this.id = messageId;
        this.sender = sender;
        this.parentMessageId = parentMessageId;
        this.content = content;
        this.ttl = ttl;
        this.attachments = attachments == null ? new ArrayList<>() : new ArrayList<>(attachments);
        this.editedAt = LocalDateTime.now();
        this.roomId = roomId;
        this.roomType = roomType;
    }

    public static Message createChannelMessage(String id, User sender, String channelId, String content) {
        return new Message(id, sender, null, content, null, null, channelId, RoomType.CHANNEL);
    }

    public static Message createDmMessage(String id, User sender, String dmId, String content) {
        return new Message(id, sender, null, content, null, null, dmId, RoomType.DM);
    }

    public Message reply(String id, User sender, String content) {
        return new Message(id, sender, this.id, content, null, null, this.roomId, this.roomType);
    }

    public void edit(String content) {
        this.content = content;
        this.editedAt = LocalDateTime.now();
    }

    public void expireAfter(Long ttl) {
        this.ttl = ttl;
    }
}
