package com.mycoffeestore.vo.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车项VO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "购物车项")
public class CartItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 购物车项ID
     */
    @Schema(description = "购物车项ID", example = "101")
    private Long cartId;

    /**
     * 咖啡ID
     */
    @Schema(description = "咖啡ID", example = "1")
    private Long coffeeId;

    /**
     * 咖啡名称
     */
    @Schema(description = "咖啡名称", example = "经典美式")
    private String coffeeName;

    /**
     * 图片URL
     */
    @Schema(description = "图片URL")
    private String imageUrl;

    /**
     * 单价
     */
    @Schema(description = "单价", example = "4.50")
    private BigDecimal price;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "2")
    private Integer quantity;

    /**
     * 小计
     */
    @Schema(description = "小计", example = "9.00")
    private BigDecimal subtotal;

    /**
     * 库存
     */
    @Schema(description = "库存", example = "100")
    private Integer stock;

    /**
     * 状态
     */
    @Schema(description = "状态：0-下架，1-上架，2-售罄", example = "1")
    private Integer status;
}
