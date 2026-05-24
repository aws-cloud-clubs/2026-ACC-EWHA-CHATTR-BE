package com.acc.chattr.domain.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic chatTopic;

    public void sendMessage(Object message) {

        redisTemplate.convertAndSend(
                chatTopic.getTopic(),
                message
        );
    }
}