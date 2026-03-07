package com.mycoffeestore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoffeestore.ai.config.LlmProperties;
import com.mycoffeestore.ai.factory.ChatModelFactory;
import com.mycoffeestore.ai.modelscope.ModelScopeChatModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatModel 配置类
 * 创建 ModelScope ChatModel Bean
 *
 * 注意：由于 ModelScopeChatModel 不直接实现 Spring AI 的 ChatModel 接口，
 * 这里直接返回 ModelScopeChatModel 类型，在使用时需要适配
 *
 * @author Backend Developer
 * @since 2026-03-07
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Schema(description = "ChatModel 配置类")
@ConditionalOnProperty(prefix = "agent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChatModelConfiguration {

    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper;

    /**
     * 创建 ModelScope ChatModel Bean
     *
     * @return ModelScopeChatModel 实例
     */
    @Bean(name = "modelScopeChatModel")
    @Schema(description = "ModelScope ChatModel Bean")
    public ModelScopeChatModel modelScopeChatModel() {
        log.info("初始化 ModelScope ChatModel");

        // 创建 ChatModelFactory
        ChatModelFactory factory = new ChatModelFactory(llmProperties, objectMapper);

        // 使用工厂创建 ModelScopeChatModel
        ModelScopeChatModel chatModel = factory.createChatModel();

        log.info("ModelScope ChatModel 初始化完成");
        return chatModel;
    }
}
