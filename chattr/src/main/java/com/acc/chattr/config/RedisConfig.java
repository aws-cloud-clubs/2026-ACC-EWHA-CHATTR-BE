package com.acc.chattr.config;

import com.acc.chattr.domain.message.dto.MessageSendRequest;
import com.acc.chattr.domain.message.redis.RedisMessageSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ChannelTopic chatTopic() {
        return new ChannelTopic("chat");
    }

    @Bean
    public Jackson2JsonRedisSerializer<MessageSendRequest> messageSerializer() {
        return new Jackson2JsonRedisSerializer<>(MessageSendRequest.class);
    }

    @Bean
    public RedisTemplate<String, MessageSendRequest> redisTemplate(
            RedisConnectionFactory connectionFactory,
            Jackson2JsonRedisSerializer<MessageSendRequest> messageSerializer
    ) {
        RedisTemplate<String, MessageSendRequest> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer =
                new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        template.setValueSerializer(messageSerializer);
        template.setHashValueSerializer(messageSerializer);

        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            RedisMessageSubscriber subscriber,
            ChannelTopic chatTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, chatTopic);

        return container;
    }
}