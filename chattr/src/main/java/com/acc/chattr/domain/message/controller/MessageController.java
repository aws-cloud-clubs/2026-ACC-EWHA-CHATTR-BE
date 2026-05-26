package com.acc.chattr.domain.message.controller;

import com.acc.chattr.domain.message.dto.MessageSendRequest;
import com.acc.chattr.domain.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @MessageMapping("/messages")
    public void sendMessage(
            MessageSendRequest request,
            Principal principal
    ) {
        messageService.publishMessage(request, principal.getName());
    }
}