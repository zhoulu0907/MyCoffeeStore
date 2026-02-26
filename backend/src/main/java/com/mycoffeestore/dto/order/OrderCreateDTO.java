package com.mycoffeestore.dto.order;

import com.mycoffeestore.enums.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 创建订单请求DTO
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建订单请求")
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单类型
     */
    @Schema(description = "订单类型：dine_in-堂食，takeaway-外带，delivery-外卖", example = "dine_in", required = true)
    @NotNull(message = "订单类型不能为空")
    private OrderType orderType;

    /**
     * 订单项列表
     */
    @Schema(description = "订单项列表", required = true)
    @NotEmpty(message = "订单项不能为空")
    @Size(min = 1, message = "至少有一个订单项")
    @Valid
    private List<OrderItemDTO> items;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "少糖，少冰")
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;

    /**
     * 配送地址（外卖时必填）
     */
    @Schema(description = "配送地址")
    @Valid
    private DeliveryAddressDTO deliveryAddress;
}
