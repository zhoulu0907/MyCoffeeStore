package com.mycoffeestore.vo.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车列表VO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "购物车列表")
public class CartListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总数量
     */
    @Schema(description = "总数量", example = "5")
    private Integer totalQuantity;

    /**
     * 总金额
     */
    @Schema(description = "总金额", example = "22.50")
    private BigDecimal totalPrice;

    /**
     * 购物车项列表
     */
    @Schema(description = "购物车项列表")
    private List<CartItemVO> items;
}
