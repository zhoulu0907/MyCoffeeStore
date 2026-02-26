package com.mycoffeestore.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单列表项VO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单列表项")
public class OrderListItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    @Schema(description = "订单号", example = "ORD20240226001")
    private String orderId;

    /**
     * 订单总金额
     */
    @Schema(description = "订单总金额", example = "14.50")
    private BigDecimal totalAmount;

    /**
     * 订单类型
     */
    @Schema(description = "订单类型", example = "dine_in")
    private String orderType;

    /**
     * 订单类型名称
     */
    @Schema(description = "订单类型名称", example = "堂食")
    private String orderTypeName;

    /**
     * 订单状态
     */
    @Schema(description = "订单状态", example = "preparing")
    private String status;

    /**
     * 订单状态名称
     */
    @Schema(description = "订单状态名称", example = "制作中")
    private String statusName;

    /**
     * 订单项数量
     */
    @Schema(description = "订单项数量", example = "3")
    private Integer itemCount;

    /**
     * 订单项列表
     */
    @Schema(description = "订单项列表")
    private List<OrderItemDetailVO> items;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
