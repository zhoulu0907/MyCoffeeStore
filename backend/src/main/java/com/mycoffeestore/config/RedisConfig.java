package com.mycoffeestore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * 配置 RedisTemplate 和序列化器
 *
 * @author zhoulu
 * @since 2026-03-07
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.data.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    /**
     * 配置 RedisTemplate
     * 使用 String 序列化器作为 Key 的序列化器
     * 使用 JSON 序列化器作为 Value 的序列化器
     *
     * @param connectionFactory Redis 连接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用 String 序列化器序列化 Key
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 使用 JSON 序列化器序列化 Value
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 创建 JSON 序列化器
     * 配置 ObjectMapper 以支持 LocalDateTime 等类型
     *
     * @return JSON 序列化器
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册 JavaTimeModule 以支持 LocalDateTime 序列化
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期序列化为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
