package com.mycoffeestore.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.ai.factory.ChatModelFactory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 自动配置类
 * 自动配置 Spring AI Alibaba 相关组件
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Schema(description = "LLM 自动配置类")
@EnableConfigurationProperties(LlmProperties.class)
public class LlmAutoConfiguration {

    /**
     * 配置 ChatModelFactory Bean
     */
    @Bean
    @Schema(description = "ChatModelFactory Bean")
    public ChatModelFactory chatModelFactory(LlmProperties llmProperties, ObjectMapper objectMapper) {
        log.info("初始化 ChatModelFactory，默认提供商: {}", llmProperties.getProvider());

        // 验证配置
        validateConfiguration(llmProperties);

        return new ChatModelFactory(llmProperties, objectMapper);
    }

    /**
     * 验证 LLM 配置
     */
    private void validateConfiguration(LlmProperties properties) {
        switch (properties.getProvider()) {
            case MODELSCOPE:
                if (properties.getModelscope().getApiKey() == null || properties.getModelscope().getApiKey().isEmpty()) {
                    log.warn("ModelScope API Key 未配置，请在 application.yml 中设置 llm.modelscope.api-key");
                }
                break;

            case DASHSCOPE:
                if (properties.getDashscope().getApiKey() == null || properties.getDashscope().getApiKey().isEmpty()) {
                    log.warn("DashScope API Key 未配置，请在 application.yml 中设置 llm.dashscope.api-key");
                }
                break;

            case OPENAI:
                if (properties.getOpenai().getApiKey() == null || properties.getOpenai().getApiKey().isEmpty()) {
                    log.warn("OpenAI API Key 未配置，请在 application.yml 中设置 llm.openai.api-key");
                }
                break;
        }
    }
}
