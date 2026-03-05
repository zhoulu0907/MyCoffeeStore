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
 * LLM 用户配置实体类
 *
 * @author Backend Developer
 * @since 2026-03-05
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "LLM 用户配置实体")
@Table("mcs_llm_user_config")
public class LlmUserConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @Schema(description = "配置ID", example = "1")
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 提供商ID
     */
    @Schema(description = "提供商ID", example = "1")
    private Long providerId;

    /**
     * 加密后的 API Key
     */
    @Schema(description = "加密后的 API Key")
    private String apiKeyEncrypted;

    /**
     * 加密 IV（初始化向量）
     */
    @Schema(description = "加密 IV")
    private String apiKeyIv;

    /**
     * 配置 JSON（扩展配置）
     */
    @Schema(description = "配置 JSON")
    private String configJson;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
}
