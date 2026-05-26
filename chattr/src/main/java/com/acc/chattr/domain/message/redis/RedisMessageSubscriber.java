package com.acc.chattr.domain.message.redis;

import com.acc.chattr.domain.message.dto.MessageSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    private final Jackson2JsonRedisSerializer<MessageSendRequest>
            serializer;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            MessageSendRequest request =
                    serializer.deserialize(message.getBody());

            if (request == null) {
                log.warn("Redis 메시지 역직렬화 결과가 null입니다.");
                return;
            }

            messagingTemplate.convertAndSend(
                    "/topic/rooms/" + request.getRoomId(),
                    request
            );

        } catch (Exception e) {
            log.error("Redis 메시지 처리 중 오류 발생", e);
        }
    }
}