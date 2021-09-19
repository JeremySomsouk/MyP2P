package com.streaming.myp2p.configuration;

import com.streaming.myp2p.repository.model.StatsCacheEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Bean
    @Qualifier("StatsRedisTemplate")
    public RedisTemplate<String, StatsCacheEntity> redisStatsTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, StatsCacheEntity> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setDefaultSerializer(new Jackson2JsonRedisSerializer<>(StatsCacheEntity.class));
        return template;
    }

    @Bean
    @Qualifier("StringRedisTemplate")
    public RedisTemplate<String, String> redisStringTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setDefaultSerializer(new Jackson2JsonRedisSerializer<>(String.class));
        return template;
    }
}
