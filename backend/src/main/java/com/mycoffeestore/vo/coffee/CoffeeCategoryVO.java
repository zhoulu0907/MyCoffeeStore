package com.mycoffeestore.vo.coffee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 咖啡分类VO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "咖啡分类")
public class CoffeeCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类代码
     */
    @Schema(description = "分类代码", example = "espresso")
    private String code;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", example = "意式浓缩系列")
    private String name;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "8")
    private Integer count;
}
