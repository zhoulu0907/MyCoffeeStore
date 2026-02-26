package com.mycoffeestore.entity;

import com.mycoffeestore.common.base.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 购物车实体类
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "购物车实体")
@Table("mcs_cart")
public class Cart extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 购物车项ID
     */
    @Schema(description = "购物车项ID", example = "101")
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 咖啡ID
     */
    @Schema(description = "咖啡ID", example = "1")
    private Long coffeeId;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "2")
    private Integer quantity;

    /**
     * 添加时单价
     */
    @Schema(description = "添加时单价", example = "4.50")
    private java.math.BigDecimal price;
}
