package com.acc.chattr.domain.message.controller;

import com.acc.chattr.domain.message.dto.MessageSendRequest;
import com.acc.chattr.domain.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @MessageMapping("/messages")
    public void sendMessage(MessageSendRequest request) {
        messageService.publishMessage(request);
    }
}