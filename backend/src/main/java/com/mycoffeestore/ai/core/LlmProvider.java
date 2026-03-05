package com.mycoffeestore.ai.core;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * LLM 提供商枚举
 * 定义支持的 LLM 服务提供商类型
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Schema(description = "LLM 提供商枚举")
public enum LlmProvider {

    /**
     * ModelScope - 阿里云 ModelScope 服务
     */
    @Schema(description = "ModelScope 提供商")
    MODELSCOPE("modelscope", "阿里云 ModelScope"),

    /**
     * DashScope - 阿里云通义千问服务
     */
    @Schema(description = "DashScope 提供商")
    DASHSCOPE("dashscope", "阿里云通义千问"),

    /**
     * OpenAI - OpenAI 服务
     */
    @Schema(description = "OpenAI 提供商")
    OPENAI("openai", "OpenAI");

    private final String code;
    private final String description;

    LlmProvider(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取提供商
     *
     * @param code 提供商代码
     * @return LLM 提供商枚举
     */
    public static LlmProvider fromCode(String code) {
        for (LlmProvider provider : values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("未知的 LLM 提供商: " + code);
    }
}
