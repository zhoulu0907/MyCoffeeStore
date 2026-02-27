package com.mycoffeestore.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Modelscope WebClient 配置
 *
 * @author zhoulu
 * @since 2026-02-27
 */
@Configuration
@RequiredArgsConstructor
public class ModelScopeConfig {

    private final ModelScopeProperties properties;

    /**
     * 创建 Modelscope WebClient
     */
    @Bean
    public WebClient modelScopeWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getTimeout().intValue())
                .responseTimeout(Duration.ofMillis(properties.getTimeout()));

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }
}
