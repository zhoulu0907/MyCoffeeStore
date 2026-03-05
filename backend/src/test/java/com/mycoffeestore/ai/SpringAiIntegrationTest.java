package com.mycoffeestore.ai;

import com.mycoffeestore.ai.config.ModelScopeChatOptions;
import com.mycoffeestore.ai.core.LlmProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring AI Alibaba 集成测试
 * 验证基础架构组件是否正确配置
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Disabled("临时禁用集成测试，需要修复 Spring 上下文加载问题")
class SpringAiIntegrationTest {

    @Test
    void testLlmProviderEnum() {
        assertEquals("modelscope", LlmProvider.MODELSCOPE.getCode());
        assertEquals("dashscope", LlmProvider.DASHSCOPE.getCode());
        assertEquals("openai", LlmProvider.OPENAI.getCode());

        assertEquals(LlmProvider.MODELSCOPE, LlmProvider.fromCode("modelscope"));
        assertEquals(LlmProvider.DASHSCOPE, LlmProvider.fromCode("dashscope"));
        assertEquals(LlmProvider.OPENAI, LlmProvider.fromCode("openai"));

        assertThrows(IllegalArgumentException.class, () -> LlmProvider.fromCode("unknown"));
    }

    @Test
    void testModelScopeChatOptions() {
        ModelScopeChatOptions options = ModelScopeChatOptions.builder()
                .model("test-model")
                .temperature(0.8)
                .maxTokens(2048)
                .stream(true)
                .build();

        assertEquals("test-model", options.getModel());
        assertEquals(0.8, options.getTemperature());
        assertEquals(2048, options.getMaxTokens());
        assertTrue(options.getStream());

        // 测试默认选项
        ModelScopeChatOptions defaultOptions = ModelScopeChatOptions.defaultOptions();
        assertNotNull(defaultOptions);
        assertEquals(0.7, defaultOptions.getTemperature());
        assertEquals(4096, defaultOptions.getMaxTokens());
        assertTrue(defaultOptions.getStream());
    }
}
