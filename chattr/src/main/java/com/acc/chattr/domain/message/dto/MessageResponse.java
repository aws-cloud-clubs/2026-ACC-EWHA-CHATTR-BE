package com.acc.chattr.domain.message.dto;

import com.acc.chattr.domain.message.entity.RoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class MessageResponse {

    private String messageId;
    private String senderId;
    private String roomId;
    private RoomType roomType;
    private String content;
    private String parentMessageId;
    private List<MessageAttachmentDto> attachments;
    private Instant createdAt;
    private Instant editedAt;
}