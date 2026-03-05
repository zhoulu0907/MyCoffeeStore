package com.mycoffeestore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 测试配置类
 *
 * @author zhoulu
 * @since 2026-03-05
 */
@Configuration
public class TestConfig {

    /**
     * 创建测试用的 ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * 创建测试用的 WebClient
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://api.modelscope.cn/v1")
                .build();
    }
}