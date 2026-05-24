package com.acc.chattr.domain.message.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageAttachmentDto {

    private String fileUrl;
    private String fileName;
}