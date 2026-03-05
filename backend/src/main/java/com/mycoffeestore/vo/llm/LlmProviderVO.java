package com.mycoffeestore.vo.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 提供商 VO
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LLM 提供商信息")
public class LlmProviderVO {

    /**
     * 提供商ID
     */
    @Schema(description = "提供商ID", example = "1")
    private Long id;

    /**
     * 提供商代码
     */
    @Schema(description = "提供商代码", example = "modelscope")
    private String providerCode;

    /**
     * 提供商名称
     */
    @Schema(description = "提供商名称", example = "ModelScope 灵积")
    private String providerName;

    /**
     * 提供商类型
     */
    @Schema(description = "提供商类型", example = "modelscope")
    private String providerType;

    /**
     * API 端点
     */
    @Schema(description = "API 端点", example = "https://api-inference.modelscope.cn/v1")
    private String apiEndpoint;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    /**
     * 排序顺序
     */
    @Schema(description = "排序顺序", example = "100")
    private Integer sortOrder;

    /**
     * 是否已配置 API Key
     */
    @Schema(description = "是否已配置 API Key", example = "false")
    private Boolean hasConfigured;
}
