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

/**
 * LLM 提供商实体类
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "LLM 提供商实体")
@Table("mcs_llm_provider")
public class LlmProvider extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 提供商ID
     */
    @Schema(description = "提供商ID", example = "1")
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 提供商代码（唯一标识）
     */
    @Schema(description = "提供商代码", example = "modelscope")
    private String providerCode;

    /**
     * 提供商名称
     */
    @Schema(description = "提供商名称", example = "ModelScope 灵积")
    private String providerName;

    /**
     * 提供商类型（modelscope/dashscope/hunyuan/zhipu/baichuan/openai/anthropic/openrouter/deepseek）
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
