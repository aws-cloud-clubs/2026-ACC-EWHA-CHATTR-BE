package com.acc.chattr.domain.message.dto;

import com.acc.chattr.domain.message.entity.RoomType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MessageSendRequest {

    private String roomId;              // channel_id or dm_id
    private RoomType roomType;          // CHANNEL or DM
    private String content;
    private String parentMessageId;     // 스레드 답글이면 사용, 아니면 null
    private List<MessageAttachmentDto> attachments;
}