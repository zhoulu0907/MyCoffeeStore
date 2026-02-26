package com.mycoffeestore.entity;

import com.mycoffeestore.common.base.BaseEntity;
import com.mycoffeestore.enums.OrderStatus;
import com.mycoffeestore.enums.OrderType;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 订单实体类
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单实体")
@Table("mcs_order")
public class Order extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID", example = "1")
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 订单号
     */
    @Schema(description = "订单号", example = "ORD20240226001")
    private String orderNo;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 订单总金额
     */
    @Schema(description = "订单总金额", example = "14.50")
    private java.math.BigDecimal totalAmount;

    /**
     * 订单类型
     */
    @Schema(description = "订单类型", example = "dine_in")
    private String orderType;

    /**
     * 订单状态
     */
    @Schema(description = "订单状态", example = "preparing")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 配送地址（JSON）
     */
    @Schema(description = "配送地址")
    private String deliveryAddress;

    /**
     * 取消原因
     */
    @Schema(description = "取消原因")
    private String cancelReason;

    /**
     * 支付时间
     */
    @Schema(description = "支付时间")
    private LocalDateTime paidAt;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    /**
     * 取消时间
     */
    @Schema(description = "取消时间")
    private LocalDateTime cancelledAt;
}
