package com.acc.chattr.domain.message.dto;

public record MessageAttachmentDto(
        String fileUrl,
        String fileName
) {}