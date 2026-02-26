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
 * 订单详情实体类
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单详情实体")
@Table("mcs_order_item")
public class OrderItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 详情ID
     */
    @Schema(description = "详情ID", example = "1")
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    /**
     * 咖啡ID
     */
    @Schema(description = "咖啡ID", example = "1")
    private Long coffeeId;

    /**
     * 咖啡名称（冗余）
     */
    @Schema(description = "咖啡名称", example = "经典美式")
    private String coffeeName;

    /**
     * 咖啡图片（冗余）
     */
    @Schema(description = "咖啡图片URL")
    private String imageUrl;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "2")
    private Integer quantity;

    /**
     * 单价（下单时快照）
     */
    @Schema(description = "单价", example = "4.50")
    private java.math.BigDecimal price;

    /**
     * 小计
     */
    @Schema(description = "小计", example = "9.00")
    private java.math.BigDecimal subtotal;
}
