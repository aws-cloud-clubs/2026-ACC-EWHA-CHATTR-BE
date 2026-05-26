package com.acc.chattr.domain.message.service;

import com.acc.chattr.domain.message.dto.MessageSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic chatTopic;

    public void publishMessage(MessageSendRequest request) {
        redisTemplate.convertAndSend(chatTopic.getTopic(), request);
    }
}