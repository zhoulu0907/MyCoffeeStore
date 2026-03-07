package com.mycoffeestore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis 配置属性
 *
 * @author zhoulu
 * @since 2026-03-07
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {

    /**
     * Redis 主机地址
     */
    private String host = "localhost";

    /**
     * Redis 端口
     */
    private int port = 6379;

    /**
     * Redis 密码（可选）
     */
    private String password;

    /**
     * 连接超时时间（毫秒）
     */
    private int timeout = 3000;

    /**
     * 数据库索引
     */
    private int database = 0;

    /**
     * 对话记忆相关配置
     */
    private Memory memory = new Memory();

    @Data
    public static class Memory {
        /**
         * Redis 中消息的 TTL（秒），默认 24 小时
         */
        private long ttl = 86400;

        /**
         * 每个会话最多保留的消息数量
         */
        private int maxMessages = 100;

        /**
         * Redis Key 前缀
         */
        private String keyPrefix = "conversation:";

        /**
         * 是否启用 Redis（当 Redis 不可用时自动降级）
         */
        private boolean enabled = true;
    }
}
