package com.mycoffeestore.ai.config;

import com.mycoffeestore.ai.core.LlmProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM 通用配置属性
 * 统一管理所有 LLM 提供商的配置
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Data
@Component
@Schema(description = "LLM 通用配置属性")
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    /**
     * 默认提供商
     */
    @Schema(description = "默认 LLM 提供商")
    private LlmProvider provider = LlmProvider.MODELSCOPE;

    /**
     * ModelScope 配置
     */
    @Schema(description = "ModelScope 配置")
    private ModelScopeConfig modelscope = new ModelScopeConfig();

    /**
     * DashScope 配置
     */
    @Schema(description = "DashScope 配置")
    private DashScopeConfig dashscope = new DashScopeConfig();

    /**
     * OpenAI 配置
     */
    @Schema(description = "OpenAI 配置")
    private OpenAIConfig openai = new OpenAIConfig();

    /**
     * 通用配置
     */
    @Schema(description = "通用配置")
    private CommonConfig common = new CommonConfig();

    /**
     * ModelScope 专用配置
     */
    @Data
    @Schema(description = "ModelScope 配置")
    public static class ModelScopeConfig {
        @Schema(description = "API 密钥")
        private String apiKey;

        @Schema(description = "API 基础地址")
        private String baseUrl = "https://api-inference.modelscope.cn/v1";

        @Schema(description = "模型名称")
        private String model = "kimih/K2.5-Instruct";

        @Schema(description = "超时时间（毫秒）")
        private Long timeout = 60000L;

        @Schema(description = "最大重试次数")
        private Integer maxRetries = 3;

        @Schema(description = "温度参数")
        private Double temperature = 0.7;
    }

    /**
     * DashScope 专用配置
     */
    @Data
    @Schema(description = "DashScope 配置")
    public static class DashScopeConfig {
        @Schema(description = "API 密钥")
        private String apiKey;

        @Schema(description = "模型名称")
        private String model = "qwen-max";

        @Schema(description = "超时时间（毫秒）")
        private Long timeout = 60000L;
    }

    /**
     * OpenAI 专用配置
     */
    @Data
    @Schema(description = "OpenAI 配置")
    public static class OpenAIConfig {
        @Schema(description = "API 密钥")
        private String apiKey;

        @Schema(description = "API 基础地址")
        private String baseUrl = "https://api.openai.com/v1";

        @Schema(description = "模型名称")
        private String model = "gpt-4";

        @Schema(description = "超时时间（毫秒）")
        private Long timeout = 60000L;
    }

    /**
     * 通用配置
     */
    @Data
    @Schema(description = "通用配置")
    public static class CommonConfig {
        @Schema(description = "最大 Token 数")
        private Integer maxTokens = 4096;

        @Schema(description = "流式输出")
        private Boolean stream = true;

        @Schema(description = "是否启用缓存")
        private Boolean enableCache = true;
    }
}
