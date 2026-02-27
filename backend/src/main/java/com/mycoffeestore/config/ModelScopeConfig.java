package com.mycoffeestore.config;

import io.netty.channel.ChannelOption;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Modelscope AI 配置类
 *
 * @author zhoulu
 * @since 2026-02-27
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "modelscope")
public class ModelScopeConfig {

    /**
     * API 密钥
     */
    private String apiKey;

    /**
     * API 基础地址
     */
    private String baseUrl;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 超时时间（毫秒）
     */
    private Long timeout;

    /**
     * 创建 Modelscope WebClient
     */
    @Bean
    public WebClient modelScopeWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout.intValue())
                .responseTimeout(Duration.ofMillis(timeout));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }
}
