package com.mycoffeestore.dto.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连接测试请求 DTO
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "连接测试请求")
public class ConnectionTestRequestDTO {

    /**
     * 提供商ID
     */
    @NotNull(message = "提供商ID不能为空")
    @Schema(description = "提供商ID", example = "1")
    private Long providerId;

    /**
     * 模型ID（可选，不传则使用提供商默认模型）
     */
    @Schema(description = "模型ID", example = "1")
    private Long modelId;

    /**
     * API Key（可选，不传则使用已保存的配置）
     */
    @Schema(description = "API Key（临时测试，不保存）", example = "sk-xxxxxxxxxxxx")
    private String apiKey;
}
