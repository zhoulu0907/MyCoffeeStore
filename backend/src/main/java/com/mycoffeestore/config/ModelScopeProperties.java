package com.mycoffeestore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Modelscope AI 配置属性
 *
 * @author zhoulu
 * @since 2026-02-27
 */
@Data
@Component
@ConfigurationProperties(prefix = "modelscope")
public class ModelScopeProperties {

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
    private Long timeout = 60000L;
}
