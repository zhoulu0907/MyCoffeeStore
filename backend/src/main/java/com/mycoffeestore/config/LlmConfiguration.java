package com.mycoffeestore.config;

import com.mycoffeestore.ai.config.LlmProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 配置类
 * 启用 LlmProperties 配置属性
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@Configuration
@Schema(description = "LLM 配置类")
@EnableConfigurationProperties(LlmProperties.class)
@ConditionalOnProperty(prefix = "agent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LlmConfiguration {

    public LlmConfiguration(LlmProperties llmProperties) {
        log.info("初始化 LLM 配置");
        log.info("默认提供商: {}", llmProperties.getProvider());
        log.info("ModelScope 模型: {}", llmProperties.getModelscope().getModel());
        log.info("LLM 配置初始化完成");
    }
}
