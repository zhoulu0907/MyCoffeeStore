package com.mycoffeestore.entity;

import com.mycoffeestore.common.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * LLM 模型配置实体类
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "LLM 模型配置实体")
@Table("mcs_llm_model_config")
public class LlmModelConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模型配置ID
     */
    @Schema(description = "模型配置ID", example = "1")
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 提供商ID
     */
    @Schema(description = "提供商ID", example = "1")
    private Long providerId;

    /**
     * 模型代码（唯一标识）
     */
    @Schema(description = "模型代码", example = "kimi-k2.5")
    private String modelCode;

    /**
     * 模型名称
     */
    @Schema(description = "模型名称", example = "Kimi K2.5")
    private String modelName;

    /**
     * 模型ID（API调用使用）
     */
    @Schema(description = "模型ID", example = "moonshotai/Kimi-K2.5")
    private String modelId;

    /**
     * 模型类型（chat/vision/embedding）
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
     * 是否支持流式输出：0-不支持，1-支持
     */
    @Schema(description = "是否支持流式输出", example = "1")
    private Integer supportStreaming;

    /**
     * 是否支持函数调用：0-不支持，1-支持
     */
    @Schema(description = "是否支持函数调用", example = "1")
    private Integer supportFunctionCalling;

    /**
     * 输入定价（每千token价格，美元）
     */
    @Schema(description = "输入定价", example = "0.000012")
    private BigDecimal pricingInput;

    /**
     * 输出定价（每千token价格，美元）
     */
    @Schema(description = "输出定价", example = "0.000012")
    private BigDecimal pricingOutput;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    /**
     * 排序顺序
     */
    @Schema(description = "排序顺序", example = "100")
    private Integer sortOrder;
}
