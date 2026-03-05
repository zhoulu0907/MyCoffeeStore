package com.mycoffeestore.vo.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * LLM 模型 VO
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LLM 模型信息")
public class LlmModelVO {

    /**
     * 模型配置ID
     */
    @Schema(description = "模型配置ID", example = "1")
    private Long id;

    /**
     * 提供商ID
     */
    @Schema(description = "提供商ID", example = "1")
    private Long providerId;

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
     * 模型代码
     */
    @Schema(description = "模型代码", example = "kimi-k2.5")
    private String modelCode;

    /**
     * 模型名称
     */
    @Schema(description = "模型名称", example = "Kimi K2.5")
    private String modelName;

    /**
     * 模型ID（API 调用使用）
     */
    @Schema(description = "模型ID", example = "moonshotai/Kimi-K2.5")
    private String modelId;

    /**
     * 模型类型
     */
    @Schema(description = "模型类型", example = "chat")
    private String modelType;

    /**
     * 上下文窗口大小
     */
    @Schema(description = "上下文窗口大小", example = "32768")
    private Integer contextWindow;

    /**
     * 最大输出 token 数
     */
    @Schema(description = "最大输出 token 数", example = "8192")
    private Integer maxOutputTokens;

    /**
     * 是否支持流式输出
     */
    @Schema(description = "是否支持流式输出", example = "true")
    private Boolean supportStreaming;

    /**
     * 是否支持函数调用
     */
    @Schema(description = "是否支持函数调用", example = "true")
    private Boolean supportFunctionCalling;

    /**
     * 输入定价
     */
    @Schema(description = "输入定价（美元/千token）", example = "0.000012")
    private BigDecimal pricingInput;

    /**
     * 输出定价
     */
    @Schema(description = "输出定价（美元/千token）", example = "0.000012")
    private BigDecimal pricingOutput;

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
}
