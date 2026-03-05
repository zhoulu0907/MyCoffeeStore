package com.mycoffeestore.dto.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 配置更新请求 DTO
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LLM 配置更新请求")
public class LlmConfigUpdateRequestDTO {

    /**
     * 提供商ID
     */
    @NotNull(message = "提供商ID不能为空")
    @Schema(description = "提供商ID", example = "1")
    private Long providerId;

    /**
     * API Key
     */
    @NotBlank(message = "API Key不能为空")
    @Schema(description = "API Key", example = "sk-xxxxxxxxxxxx")
    private String apiKey;

    /**
     * 扩展配置（JSON 字符串）
     */
    @Schema(description = "扩展配置（JSON 字符串）", example = "{\"temperature\": 0.7}")
    private String configJson;
}
