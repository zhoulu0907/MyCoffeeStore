package com.mycoffeestore.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单项详情VO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单项详情")
public class OrderItemDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单项ID
     */
    @Schema(description = "订单项ID", example = "1")
    private Long itemId;

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
     * 数量
     */
    @Schema(description = "数量", example = "2")
    private Integer quantity;

    /**
     * 单价
     */
    @Schema(description = "单价", example = "4.50")
    private BigDecimal price;

    /**
     * 小计
     */
    @Schema(description = "小计", example = "9.00")
    private BigDecimal subtotal;
}
